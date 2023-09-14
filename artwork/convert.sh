#!/bin/bash

set -ex

inkscape -C -o banner.png banner.svg
inkscape -C -o dialog.png dialog.svg

convert banner.png ../desktop/src/main/packaging/windows/banner.bmp
convert dialog.png ../desktop/src/main/packaging/windows/dialog.bmp

inkscape -C -o icon16.png -h 16 icon.svg
inkscape -C -o icon32.png -h 32 icon.svg
inkscape -C -o icon48.png -h 48 icon.svg
inkscape -C -o icon192.png -h 192 icon.svg
inkscape -C -o icon256.png -h 256 icon.svg
convert icon16.png icon32.png icon48.png icon256.png ../desktop/src/main/packaging/windows/compose-browser.ico

inkscape -C -h 500 -o ../desktop/src/main/packaging/deb/compose-browser.png icon.svg

cp icon192.png ../desktop/src/main/resources/compose-browser.png
