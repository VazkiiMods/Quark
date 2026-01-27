package org.violetmoon.quark.catnip.net.base;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CatnipPacketRegistry {
	public final String modId;
	public final String networkVersion;

	private final Set<PacketType<?>> packets = new HashSet<>();
	public final Set<PacketType<?>> packetsView = Collections.unmodifiableSet(packets);

	private boolean packetsRegistered = false;

	public CatnipPacketRegistry(String modId, int networkVersion) {
		this(modId, String.valueOf(networkVersion));
	}

	public CatnipPacketRegistry(String modId, String networkVersion) {
		this.modId = modId;
		this.networkVersion = networkVersion;
	}

	public void registerPacket(PacketType<?> packetType) {
		if (packetsRegistered)
			throw new IllegalStateException("Cannot register more packets after registerAllPackets() has been called!");

		packets.add(packetType);
	}

	public void registerAllPackets() {
		if (packetsRegistered)
			throw new IllegalStateException("Cannot call registerAllPackets() more than once!");

		ModContainer container = ModList.get().getModContainerById(modId).orElseThrow();
		container.getEventBus().addListener((RegisterPayloadHandlersEvent e) -> {
			PayloadRegistrar registrar = e.registrar(networkVersion);

			for (CatnipPacketRegistry.PacketType<?> type : packetsView) {
				boolean clientbound = ClientboundPacketPayload.class.isAssignableFrom(type.clazz());
				boolean serverbound = ServerboundPacketPayload.class.isAssignableFrom(type.clazz());
				if (clientbound && serverbound) {
					throw new IllegalStateException("Packet class is both clientbound and serverbound: " + type.clazz());
				} else if (clientbound) {
					CatnipPacketRegistry.PacketType<ClientboundPacketPayload> casted = (CatnipPacketRegistry.PacketType<ClientboundPacketPayload>) type;
					registrar.playToClient(casted.type(), casted.codec(), (payload, ctx) -> {
						ctx.enqueueWork(() -> {
							payload.handleInternal(ctx.player());
						});
					});
				} else if (serverbound) {
					CatnipPacketRegistry.PacketType<ServerboundPacketPayload> casted = (CatnipPacketRegistry.PacketType<ServerboundPacketPayload>) type;
					registrar.playToServer(casted.type(), casted.codec(), (payload, ctx) -> {
						ctx.enqueueWork(() -> {
							payload.handle((ServerPlayer) ctx.player());
						});
					});
				}
			}
		});
		packetsRegistered = true;
	}

	public record PacketType<T extends BasePacketPayload>(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {}
}
