package vip.zhijiakeji.player.entiey

/**
 * 小说信息
 * [novelName]  小说名称
 * [novelDis]   小说描述
 * [voiceList]  小说剧集
 */
class NovelInfo constructor(
    var novelPath: String,
    var novelName: String,
    var novelDis: String,
    var voiceList: ArrayList<String>
) {
}