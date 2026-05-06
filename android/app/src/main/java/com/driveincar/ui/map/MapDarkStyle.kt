package com.driveincar.ui.map

/**
 * Google Maps Android SDK 다크 스타일 JSON. Apex Lines 의 매트한 검정 톤에 맞춤.
 * 색상은 ApexColors 와 일치 (bg #0B0D12, bgRaised #14171F).
 *
 * 적용 방법: GoogleMap composable 의 properties = MapProperties(mapStyleOptions = ...)
 * 에 MapStyleOptions(MAP_STYLE_DARK_JSON) 을 넣어준다.
 */
const val MAP_STYLE_DARK_JSON: String = """
[
  {"elementType":"geometry","stylers":[{"color":"#0B0D12"}]},
  {"elementType":"labels.icon","stylers":[{"visibility":"off"}]},
  {"elementType":"labels.text.fill","stylers":[{"color":"#818797"}]},
  {"elementType":"labels.text.stroke","stylers":[{"color":"#0B0D12"}]},
  {"featureType":"administrative","elementType":"geometry","stylers":[{"color":"#2A2D36"}]},
  {"featureType":"administrative.country","elementType":"labels.text.fill","stylers":[{"color":"#B1B6C4"}]},
  {"featureType":"administrative.locality","elementType":"labels.text.fill","stylers":[{"color":"#B1B6C4"}]},
  {"featureType":"administrative.neighborhood","stylers":[{"visibility":"off"}]},
  {"featureType":"poi","stylers":[{"visibility":"off"}]},
  {"featureType":"poi.park","elementType":"geometry","stylers":[{"color":"#14171F"}]},
  {"featureType":"road","elementType":"geometry","stylers":[{"color":"#14171F"}]},
  {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#1B1F29"}]},
  {"featureType":"road","elementType":"labels.text.fill","stylers":[{"color":"#818797"}]},
  {"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#1B1F29"}]},
  {"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#3C404B"}]},
  {"featureType":"transit","stylers":[{"visibility":"off"}]},
  {"featureType":"water","elementType":"geometry","stylers":[{"color":"#080A0F"}]},
  {"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#3C404B"}]}
]
"""
