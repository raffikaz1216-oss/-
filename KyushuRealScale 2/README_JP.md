# Kyushu Real Scale MOD 0.1.0 (試作)

Minecraft Java 1.20.1 / Forge 47.4.28 / Java 17 向けのソースプロジェクトです。

## 何をするMOD？

16x16ピクセルのPNG地形タイルを読み込み、Minecraftに地形を配置します。

- 横方向: PNG 1ピクセル = Minecraft 1ブロック = 現実の1メートル
- 不透明ピクセル: 陸地
- 透明ピクセル: 海面の水だけを配置（海底は作りません）
- 陸地の明るさ: 標高を表す（黒=Y64、白=Y224）
- 線路・駅・トンネルは生成しません。自分で建設する前提です。

## 重要な制限

これは**試作版**です。完成した九州の地形データや完成済みJARは含みません。
国土地理院などから取得した標高データを、別途この形式のPNGに変換する必要があります。
この版は地形をチャンク生成時に自動生成するものではなく、コマンドで16x16ブロックずつ配置します。
九州全域を一度に生成する用途にはまだ最適化されていません。

また、Minecraftの高さ制限に合わせ、標高はY=64〜224に圧縮しています。
水平距離だけが1:1です。現実の標高を垂直方向も1m=1ブロックで表現するものではありません。

## インストール（開発用）

1. Java 17をインストール。
2. Gradle 8系を用意。
3. このフォルダで `gradle build` を実行。
4. `build/libs/KyushuRealScale-0.1.0.jar` をForge 1.20.1プロファイルの `mods` に入れる。
5. Forge 1.20.1でワールドを起動する。

このZIPはソース一式です。ビルド済みJARではありません。

## PNGタイルの準備

1. 16x16ピクセルのRGBA PNGを作る。
2. 陸地は不透明、海は透明にする。
3. 陸地のグレースケール値を標高に対応させる。
4. `config/kyushurealscale/tiles/` に `0_0.png` のような名前で置く。

大きなPNGを16x16タイルに分割する補助スクリプト:
`tools/split_heightmap.py`

Python Pillowが必要:
`python3 -m pip install pillow`

例:
`python3 tools/split_heightmap.py kyushu_heightmap.png ~/Library/Application\ Support/CurseForge/minecraft/Instances/PROFILE/config/kyushurealscale/tiles`

※ `PROFILE` は実際のCurseForgeプロファイル名に置き換えてください。

## ゲーム内コマンド

OP権限またはチートONで実行:

`/kyushu generate 0 0`

指定したタイル `config/kyushurealscale/tiles/0_0.png` を、ワールド座標X=0〜15、Z=0〜15に生成します。

タイル座標はワールド座標に対応します:
- `0_0.png` → X 0..15, Z 0..15
- `1_0.png` → X 16..31, Z 0..15
- `-1_0.png` → X -16..-1, Z 0..15

**最初は必ず新規のテストワールドで試してください。既存の建築物を上書きする場合があります。**

## 次の開発段階

- GeoTIFF/DEMからPNGタイルを直接作る変換ツール
- 地理座標とMinecraft座標の対応付け
- 近隣タイルの一括生成と進捗表示
- チャンク生成時の自動地形生成
- 海岸線・川の水位や地表ブロックの改善
- MTRでの走行テスト
