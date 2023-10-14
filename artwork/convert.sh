#!/bin/bash

set -ex

pinpit create-image-assets-from-material-icon --input symbol.svg \
    --output . --color-background 0x41c300 --size-symbol 0.6

mv icon-192.png ../desktop/src/main/resources/compose-browser.png

mkdir -p ../desktop/src/main/packaging/linux/
mv icon-500.png ../desktop/src/main/packaging/linux/compose-browser.png

mkdir -p ../desktop/src/main/packaging/windows/
mv banner.bmp ../desktop/src/main/packaging/windows/
mv dialog.bmp ../desktop/src/main/packaging/windows/
mv icon.ico ../desktop/src/main/packaging/windows/compose-browser.ico

mkdir -p ../desktop/src/main/packaging/macos/
mv icon.icns ../desktop/src/main/packaging/macos/compose-browser.icns
