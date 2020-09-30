import org.docheinstein.commons.http.HttpDownloader;
import org.docheinstein.commons.thread.ThreadUtil;

import java.io.IOException;


public class VVVVIDDownloadTest {
    private static final String[] segments = new String[] {
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment1_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment2_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment3_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment4_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment5_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment6_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment7_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment8_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment9_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment10_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment11_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment12_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment13_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment14_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment15_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment16_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment17_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment18_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment19_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment20_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment21_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment22_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment23_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment24_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment25_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment26_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment27_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment28_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment29_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment30_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment31_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment32_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment33_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment34_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment35_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment36_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment37_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment38_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment39_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment40_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment41_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment42_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment43_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment44_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment45_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment46_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment47_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment48_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment49_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment50_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment51_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment52_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment53_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment54_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment55_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment56_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment57_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment58_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment59_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment60_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment61_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment62_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment63_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment64_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment65_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment66_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment67_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment68_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment69_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment70_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment71_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment72_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment73_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment74_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment75_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment76_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment77_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment78_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment79_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment80_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment81_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment82_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment83_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment84_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment85_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment86_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment87_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment88_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment89_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment90_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment91_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment92_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment93_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment94_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment95_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment96_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment97_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment98_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment99_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment100_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment101_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment102_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment103_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment104_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment105_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment106_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment107_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment108_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment109_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment110_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment111_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment112_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment113_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment114_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment115_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment116_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment117_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment118_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment119_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment120_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment121_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment122_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment123_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment124_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment125_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment126_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment127_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment128_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment129_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment130_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment131_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment132_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment133_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment134_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment135_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment136_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment137_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment138_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment139_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment140_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment141_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment142_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment143_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment144_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment145_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment146_0_av.ts?null=0",
            "https://vvvvid-vh.akamaihd.net/i/Dynit/FullMetalPanic/FullMetalPanic_S04Ep02_NGsnS5YBWMJjVnCgm.mp4/segment147_0_av.ts?null=0",
    };
    public static void main(String[] args) {

        int i = 0;
        for (String segment: segments) {
            System.out.println(">> Segment: " + segment);

            String filename = "/tmp/vvvid/" + i + ".ts";

            try {
                int finalI = i;
                boolean downloaded = new HttpDownloader().download(
                        segment,
                        filename,
                        downloadedBytes -> {
                            System.out.println("[" + finalI +  "] progress: " + downloadedBytes);
                        }, 1000
                );

                if (downloaded) {
                    System.out.println("DOWNLOADED! (sleeping)");
                    ThreadUtil.sleep(1000);
                } else {
                    System.err.println("Download failed");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            i++;
        }

    }
}
