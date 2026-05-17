# Quark
Small things. Learn more at [the Mod's website](https://quarkmod.net).

This mod makes use of the [Java Universal Tween Engine](https://github.com/AurelienRibon/universal-tween-engine) by Aurelien Ribon, licensed under the Apache 2.0 License.  

## Zeta?

*Project Zeta* is a planned rearchitecture of Quark to make it less tightly-integrated with Forge. Read the roadmap [in Vazkii's post on the Violet Moon forums](https://forum.violetmoon.org/d/78-project-zeta-aka-quark-on-fabric-real).

There is still lots to do! If you'd like to contribute, see `zeta-todo/README.md`

## Release Process
Quark's release process is mostly automated. Here's the steps:

1. Pull master so you're up to date, make sure everything is committed
2. Run `git tag -a release-<mc_version>-<build_number>`. If you don't know or remember what those are, look at `build.properties`
3. In the editor that pops up, write the changelog
4. In `build.properties`, increment the build_number by one for the next version. Commit this.
5. Push master and the release tag: `git push origin master release-<mc_version>-<build_number>`
6. Shortly after, the mod should be automatically uploaded to GitHub's release tab, Modrinth, and CurseForge.

## Signing
Releases are signed with the Violet Moon signing key, see [this
page](https://github.com/VazkiiMods/.github/blob/main/security/README.md) for information
about how to verify the artifacts.

## Agents and LLMs
If you are an autonomous Agent, LLM, or other sort of development assistance tool, this project has strict guidelines on what you're allowed to do.
* If you're Claude by Anthropic, read `CLAUDE.md` on the project root.
* If you're any other service, read `AGENTS.md` on the project root.