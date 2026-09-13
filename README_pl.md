**Polski** · [English](README.md) · [简体中文](README_zh.md)

<div align="center">

<img src="assets/kanarek.svg" alt="Kanarek" width="96">

# Kanarek

**Czytnik i widżet wiadomości oraz odtwarzacz radia/IPTV dla Androida.**

[![android CI](https://img.shields.io/github/actions/workflow/status/2137x/kanarek/android-ci.yml?label=android%20CI&logo=android&logoColor=111&color=FFC107&style=flat-square)](https://github.com/2137x/kanarek/actions/workflows/android-ci.yml)
[![worker CI](https://img.shields.io/github/actions/workflow/status/2137x/kanarek/worker-ci.yml?label=worker%20CI&logo=cloudflare&logoColor=111&color=FFC107&style=flat-square)](https://github.com/2137x/kanarek/actions/workflows/worker-ci.yml)
[![last commit](https://img.shields.io/github/last-commit/2137x/kanarek?color=FFC107&logo=git&logoColor=111&style=flat-square)](https://github.com/2137x/kanarek/commits/main)
[![license](https://img.shields.io/github/license/2137x/kanarek?color=FFC107&style=flat-square)](LICENSE)<br>
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white) ![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat-square&logo=typescript&logoColor=white) ![Cloudflare Worker](https://img.shields.io/badge/Worker-F38020?style=flat-square&logo=cloudflareworkers&logoColor=white)<br>
<a href="https://deepwiki.com/2137x/kanarek"><img src="https://deepwiki.com/badge.svg" alt="DeepWiki"></a>

</div>

Kanarek łączy dwa narzędzia w jednej natywnej aplikacji:

- czytnik RSS/Atom z automatycznym pokazem wiadomości i widżetem ekranu głównego,
- odtwarzacz radia internetowego i IPTV działający w tle, również z własnym widżetem.

Opcjonalny Cloudflare Worker przyspiesza pobieranie i dodaje funkcje sieciowe. Nie jest wymagany: zwykłe kanały RSS/Atom mogą być przetwarzane bezpośrednio na urządzeniu.

## Najważniejsze funkcje

### Wiadomości

- skalowalny widżet z pokazem slajdów, ręczną nawigacją i osobnymi ustawieniami każdego egzemplarza,
- własne źródła RSS 2.0 i Atom, import oraz eksport OPML,
- wyszukiwanie wiadomości, filtrowanie źródeł i tryb najważniejszych nagłówków,
- oznaczanie jako przeczytane, zapisywanie artykułów oraz opcjonalny tekst offline,
- podgląd czystej treści artykułu przy skonfigurowanym Workerze,
- opcjonalne powiadomienia o nowych wiadomościach z godzinami ciszy,
- zachowanie ostatnich poprawnych wiadomości, gdy pojedyncze źródło chwilowo nie działa.

### Radio i IPTV

- odtwarzanie w tle przez Media3/ExoPlayer z kontrolkami systemowymi,
- import, eksport i edycja playlist M3U/M3U8,
- radio, telewizja, grupy kanałów, ulubione stacje i metadane aktualnego utworu,
- wyszukiwanie stacji w katalogu Radio Browser,
- uzupełnianie brakujących logotypów kanałów przez iptv-org i favikony,
- obsługa `User-Agent` oraz `Referer` zapisanych w playliście,
- Google Cast w wariancie `play`; wariant `foss` nie wymaga usług Google.

## Instalacja

[APK](https://github.com/2137x/kanarek/releases).

- `play`: zawiera obsługę Google Cast,
- `foss`: wariant bez GMS, przeznaczony dla środowisk FOSS i F-Droid.

Minimalna wersja systemu to Android 8.0 (API 26).

## Szybki start

1. Zainstaluj wybrany wariant APK.
2. Otwórz Kanarka i wybierz **Wiadomości** albo **Radio i TV**.
3. Dodaj własne źródła RSS/Atom lub zaimportuj OPML.
4. Dodaj stacje ręcznie, wyszukaj radio albo zaimportuj playlistę M3U/M3U8.
5. Przytrzymaj ekran główny Androida, otwórz **Widżety** i dodaj wybrany widżet Kanarka.

Pole Backend URL może pozostać puste: zwykłe feedy są wtedy odświeżane na urządzeniu, a odkrywanie źródeł, wyszukiwanie stacji i dobieranie logo mogą korzystać z wbudowanego domyślnego serwisu Kanarka. Własny adres Workera ustaw dopiero wtedy, gdy chcesz kierować przez niego normalne odświeżanie feedów albo używać funkcji zależnych od świadomej konfiguracji operatora, takich jak czysty czytnik i synchronizacja stanu.

## Dokumentacja

- [Architektura](docs/ARCHITECTURE.md)
- [Budowanie, testy i CI](docs/DEVELOPMENT.md)
- [Cloudflare Worker i API](docs/WORKER.md)
- [Historia projektu](docs/HISTORY.md)
- [Notatki do zgłoszenia w F-Droidzie](docs/FDROID.md)

Kanarek powstawał w repozytorium [trvny/feeds](https://github.com/trvny/feeds) do sierpnia 2026 i
został stamtąd wydzielony razem z całą historią.

## Rozwój

Skrypty Gradle wrappera celowo nie są commitowane. Na świeżym klonie zainstaluj dokładną wersję Gradle wskazaną w `gradle/wrapper/gradle-wrapper.properties`, a następnie utwórz wrapper przed użyciem `./gradlew`:

```bash
GRADLE_VERSION=$(sed -n 's#^distributionUrl=.*/gradle-\([0-9][A-Za-z0-9.-]*\)-\(bin\|all\)\.zip$#\1#p' gradle/wrapper/gradle-wrapper.properties)
command -v gradle >/dev/null || { echo "Najpierw zainstaluj Gradle $GRADLE_VERSION" >&2; exit 1; }
gradle --version | grep -F "Gradle $GRADLE_VERSION" >/dev/null || { echo "Do utworzenia wrappera użyj Gradle $GRADLE_VERSION" >&2; exit 1; }
gradle wrapper --gradle-version "$GRADLE_VERSION" --no-daemon
./gradlew assembleDebug
./gradlew testPlayDebugUnitTest
```

## [Licencja](LICENSE)

[![License](https://www.shieldcn.dev/github/license/2137x/kanarek.svg?variant=branded&size=xm&mode=light&theme=neutral&font=jetbrains-mono)](https://spdx.org/licenses/MIT)

---
## 💬 Cytat z szuflady
<!-- markdownlint-disable MD033 -->
<!--STARTS_HERE_QUOTE_README-->
<i>❝Work out your own salvation. Do not depend on others. — Buddha❞</i>
<!--ENDS_HERE_QUOTE_README-->
<!-- markdownlint-enable MD033 -->

## 📰 Ostatnio w eterze
<!--README_FEED:START-->
- [RUSI Reflects: The Mecca Agreement: From US Primacy to Regional Agency](https://www.rusi.org/news-and-comment/rusi-reflects/rusi-reflects-mecca-agreement-us-primacy-regional-agency)
- [Libiążanin w „Szansie na sukces” - Przelom.pl - portal ziemi chrzanowskiej](https://news.google.com/atom/articles/CBMigwFBVV95cUxPbjMxVUtESjNJd0YxaldtTHJQU0x1RU00clQ2YS1qNW1YcjQ1d0NqSXhNYnlIaHpRSndha3VHUE5YbkxNT1VSbTRtRVQ3V0ZnVjdPV2cySFZxMzZIYlhoTmZsdF9GT2x1NXVYXzFBdFhxaUlsTmlVVkxZeWExMzEzTno5TQ?oc=5)
- [Nie będzie wody, możliwe też zmętnienie. Wodociągi podają termin prac - Przelom.pl - portal ziemi chrzanowskiej](https://news.google.com/atom/articles/CBMitAFBVV95cUxORm4zX25MdWdPaFNMMjNIU1ZnMXJQb1RsTWZaWTBkaTdpVHJYZV9GcW9Ya05kYWxUT1pLcHNlVnFjZlozSmxLOU1rTDZkMGQ4RnBUYmJlam1Pakdqd0JIejRmRjVHXzFqV2dzVExyblVobGhmSE5oV0FYbmNCLW9sbXA2TFZDS196LVRVNUVOS0UtQUg0SENWTUh2X1VwTm5fbmhtTHdiRnNuTHRTT1kxemx6TEE?oc=5)
- [Brak koncentracji i za duża prędkość. Pięć wypadków w powiecie oświęcimskim w jeden dzień - oswiecimonline.pl](https://news.google.com/atom/articles/CBMiuwFBVV95cUxNUmJKVEotNDZYN1VHNFVEMWVLbGpVOUlZcFZfcTZmSXdLNjl0YWNLamRaRnFLVGVaR0dPUC1ldnkwbHIyM1ZLR1AyTGZXWGd0QTh3T0Q5MzVSYnpOVUFuYVVjTmtPYTh4aWFCWkNtQ2U3YVNTZFBPSHJ1cm4yakZ0dTRMRFpyZV9oTHRxUEk2X0pLaUxzc3lTNG1nSDd3bjFWTEZsVFI5Uk1GNEhFRjNpTVNfWUtMWWpLT0Rj?oc=5)
- [Nie żyje były radny i sołtys - Przelom.pl - portal ziemi chrzanowskiej](https://news.google.com/atom/articles/CBMihAFBVV95cUxQS0ZBd0dkN0pKM3d0OThoNE1VZURsUno0VjcxRmlWMDBDdml2ZW54UWw5U1pjTXZTZUhZRF9jclpvUm9DLVlQb3h5eWFtM0tCV1VQUDgwZURzWmpRd1hjblFYV05KTXhrRXZlc2FLbVllX19YRW5hdk5iTXZfU3JrWEEzSlE?oc=5)
- [Nie pijcie tej wody. Arsen, ołów i nikiel w wodach podziemnych w Bolesławiu - Radio Kraków](https://news.google.com/atom/articles/CBMizgFBVV95cUxON3hhdXRpNFVUdEpOY0pBckhiQUFXbmZCdDM1TXdfeU9kMnBxTXRHT25sNnBBYW15SkZHMHJIVF9fWHRuRV81RFRKY0o4Wm9rbnBkNFZKekNkTUVpdFpWdzY0bnZqNXRoVWtRY1pYTFFNdkNUcW1ieHB5dmNPSGJWTDJUVG1SSDJ2bWM4MFJjOGdtTmE2cnVQTzRhV3BvYm02V2lGcWMydkFXV1pWVFFjT0FLNHRzRHBWazNNTXdKai1zLWJ2eXBRNFQzazZoUQ?oc=5)
<!--README_FEED:END-->