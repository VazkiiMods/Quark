#!/usr/bin/env bash
set -euo pipefail

# Remove 'refs/tags/' from front
TAGNAME="release-${VERSION}+${MINECRAFT_VERSION}"

function release_github() {
	echo >&2 'Creating GitHub Release'
	local GH_RELEASE_RESPONSE
	GH_RELEASE_RESPONSE="$(gh api \
	   --method POST \
	   -H "Accept: application/vnd.github+json" \
	   -H "X-GitHub-Api-Version: 2022-11-28" \
	   /repos/VazkiiMods/Zeta/releases \
	   -f tag_name="${TAGNAME}")"
	GH_RELEASE_PAGE=$(echo "$GH_RELEASE_RESPONSE" | jq -r .html_url)

	echo >&2 'Uploading Neoforge Jar and Signature to GitHub'
	gh release upload "${TAGNAME}" "${NEOFORGE_JAR}#Neoforge Jar"
	gh release upload "${TAGNAME}" "${NEOFORGE_JAR}.asc#Neoforge Signature"
}


function release_modrinth() {
	echo >&2 'Uploading Neoforge Jar to Modrinth'
	local MODRINTH_NEOFORGE_SPEC
	MODRINTH_NEOFORGE_SPEC=$(cat <<EOF
{
	"dependencies": [
			{
			"project_id": "MVARlG2f",
			"dependency_type": "required"
	}],
	"version_type": "release",
	"loaders": ["neoforge"],
	"featured": false,
	"project_id": "qnQsVE2z",
	"file_parts": [
		"jar"
	],
	"primary_file": "jar"
}
EOF
					   )

	MODRINTH_NEOFORGE_SPEC=$(echo "${MODRINTH_NEOFORGE_SPEC}" | \
							  jq --arg name "${VERSION}" \
								 --arg mcver "${MINECRAFT_VERSION}" \
								 --arg changelog "${GH_RELEASE_PAGE}" \
								 '.name=$ARGS.named.name | .version_number=$ARGS.named.name | .game_versions=[$ARGS.named.mcver] | .changelog=$ARGS.named.changelog')
	curl 'https://api.modrinth.com/v2/version' \
		 -H "Authorization: $MODRINTH_TOKEN" \
		 -F "data=$MODRINTH_NEOFORGE_SPEC" \
		 -F "jar=@${NEOFORGE_JAR}" # TODO modrinth doesn't allow asc files. Remember to readd "signature" to the spec when reenabling this. \ -F "signature=@${NEOFORGE_JAR}.asc"
}

function release_curseforge() {
	# Java versions, Loaders, and Environment tags are actually "game versions" (lmfao), as are real game versions.

	# Hardcoded from https://minecraft.curseforge.com/api/game/version-types (which is undocumented, btw)
	# I'm not betting on these changing any time soon, so hardcoding is ok
	local CURSEFORGE_JAVA_VERSION=11135 # Java 21
	local CURSEFORGE_NEOFORGE_VERSION=10150
	local CURSEFORGE_CLIENT_VERSION=9638
	local CURSEFORGE_SERVER_VERSION=9639
	# For the Minecraft one, don't hardcode so we don't have to remember to come change this every time.
	# Each game version seems to be duplicated three times:
	# Once with type ID 1 (unused?), once with its major-version-specific type ID, and once with the type ID for "Addons" 615
	# We want the second one. Just dirtily pluck it out based on this.
	local CURSEFORGE_GAME_VERSION
	CURSEFORGE_GAME_VERSION=$(curl https://minecraft.curseforge.com/api/game/versions \
								   -H 'Accept: application/json' \
								   -H "X-Api-Token: ${CURSEFORGE_TOKEN}" | \
								  jq --arg mcver "${MINECRAFT_VERSION}" \
									 'map(select(.name == $ARGS.named.mcver and .gameVersionTypeID != 1 and .gameVersionTypeID != 615)) | first | .id')

	echo >&2 'Uploading Neoforge Jar to CurseForge'
	local CURSEFORGE_NEOFORGE_SPEC
	CURSEFORGE_NEOFORGE_SPEC=$(cat <<EOF
{
    "changelogType": "text",
    "releaseType": "release"
}
EOF
						 )

	local CURSEFORGE_NEOFORGE_GAMEVERS="[\
$CURSEFORGE_JAVA_VERSION,\
$CURSEFORGE_CLIENT_VERSION,\
$CURSEFORGE_SERVER_VERSION,\
$CURSEFORGE_NEOFORGE_VERSION,\
$CURSEFORGE_GAME_VERSION]"

	CURSEFORGE_NEOFORGE_SPEC=$(echo "$CURSEFORGE_NEOFORGE_SPEC" | \
								jq --arg changelog "$GH_RELEASE_PAGE" \
								   --argjson gamevers "$CURSEFORGE_NEOFORGE_GAMEVERS" \
								   '.gameVersions=$ARGS.named.gamevers | .changelog=$ARGS.named.changelog')
	curl 'https://minecraft.curseforge.com/api/projects/243121/upload-file' \
		 -H "X-Api-Token: $CURSEFORGE_TOKEN" \
		 -F "metadata=$CURSEFORGE_NEOFORGE_SPEC" \
		 -F "file=@$NEOFORGE_JAR"
	# TODO: Upload the asc as an 'Additional file'
}

release_github
release_modrinth
release_curseforge
