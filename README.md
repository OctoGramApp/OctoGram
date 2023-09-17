<img src="https://raw.githubusercontent.com/OctoGramApp/website/main/assets/readme.images/applogo.png" width="150" align="left"/>

# 🐙 OctoGram
*[Licensed under the GNU General Public License v2.0](https://github.com/OctoGramApp/OctoGram/blob/main/LICENSE)*

[![Updates](https://img.shields.io/badge/Updates-Telegram-blue.svg)](https://t.me/octogramapp)
[![Support](https://img.shields.io/badge/Support-Telegram-blue.svg)](https://t.me/octogramchat)
![Latest](https://img.shields.io/github/v/release/OctoGramApp/OctoGram?display_name=tag&include_prereleases)
![Downloads](https://img.shields.io/github/downloads/OctoGramApp/OctoGram/total)

OctoGram is an open-source messaging platform that serves as an alternative to Telegram.<br></br>

## Compilation guide
To reproduce the build of OctoGram, you only require the presence of ccache, which is located in the "Tools" folder. On macOs, if you have Homebrew installed, it will automatically utilize the Homebrew-installed ccache; otherwise, it will use the one located in the "Tools" folder.

1. [**Obtain your own api_id**](https://core.telegram.org/api/obtaining_api_id) for your application and put it [**here**](https://github.com/OctoGramApp/OctoGram/blob/main/TMessagesProj/src/main/java/org/telegram/messenger/BuildVars.java).
2. Please **do not** use the name Telegram for your app — or make sure your users understand that it is unofficial.
3. Kindly **do not** use our standard logo (white paper plane in a blue circle) as your app's logo.
4. Please study our [**security guidelines**](https://core.telegram.org/mtproto/security_guidelines) and take good care of your users' data and privacy.
5. Please remember to publish **your** code too in order to comply with the licences.
6. Add your google-services.json file to the [**root of the project**](https://github.com/OctoGramApp/OctoGram/tree/main/TMessagesProj_App).
7. Add the following to your `local.properties` file:
```
MAPS_API_KEY=<your-api-key>
```
8. Add the following to your `signing.properties` file:
```
storePassword=<your-keystore-password>
keyAlias=<your-keystore-alias>
keyPath=<your-keystore-file-path>
keyPassword=<your-keystore-password>
```

## Thanks to the following projects:
- [Telegram](https://github.com/DrKLO/Telegram)
- [Catogram X](https://github.com/CatogramX/CatogramX) for the UI toolkit (which OctoGram's one is heavily inspired by)
- [OwlGram](https://github.com/OwlGramDev/OwlGram) for the CameraX implementation
