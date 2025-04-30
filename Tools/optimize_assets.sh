#!/bin/bash

set -e

RESET='\033[0m'
BOLD='\033[1m'
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[0;37m'
BOLD_RED='\033[1;31m'
BOLD_GREEN='\033[1;32m'
BOLD_YELLOW='\033[1;33m'
BOLD_BLUE='\033[1;34m'
BOLD_MAGENTA='\033[1;35m'
BOLD_CYAN='\033[1;36m'
BOLD_WHITE='\033[1;37m'

png_files=(
    "./TMessagesProj/src/main/res/drawable-xhdpi/icon_7_launcher_background.png"
    "./TMessagesProj/src/main/res/drawable/ic_unsized_octo.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/icon_7_launcher_background.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/icon_7_launcher_background.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/icon_7_launcher_background.png"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/icon_7_launcher_background.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_7_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_7_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_7_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_7_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_7_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_7_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_7_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_7_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_7_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_7_launcher.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/bug_report.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/datacenter_status.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/edit_passcode.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/ev_minus.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/ev_plus.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_airplane.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_all.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_book.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_bot.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_cat.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_channel.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_crown.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_custom.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_favorite.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_flower.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_game.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_groups.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_home.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_light.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_like.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_love.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_mask.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_money.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_note.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_palette.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_party.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_private.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_setup.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_sport.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_study.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_trade.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_travel.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_unmuted.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_unread.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/filter_work.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/ic_aurora_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/ic_aurora_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/ic_flora_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/ic_flora_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/ic_pluto_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/ic_pluto_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/ic_venus_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/ic_venus_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/ic_vesta_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/ic_vesta_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/icon_6_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/icon_7_launcher_background.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/icon_9_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/icon_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/icon_octo_monet_background.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/icon_yuki_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/intro_octo.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/menu_bookmarks_cn.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/menu_broadcast_cn.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/menu_calls_cn.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/menu_contacts_cn.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/menu_groups_cn.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/menu_invite_cn.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/menu_nearby_cn.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/menu_secret_cn.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/menu_settings_cn.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/msg_edited.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/msg_forward_quote.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/notification.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/octo_notification.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/outline_science_white.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/outline_source_white_28.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/round_auto_fix_high_black.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/round_bedtime_black.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/round_hdr_on_black.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/round_info_white.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/round_landscape_black.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/round_photo_camera_black.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/round_update_white_28.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/settings_appearance.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/video_quality4.png"
    "./TMessagesProj/src/main/res/drawable-hdpi/video_quality5.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/bug_report.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/datacenter_status.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/edit_passcode.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/ev_minus.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/ev_plus.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_airplane.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_all.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_book.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_bot.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_cat.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_channel.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_crown.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_custom.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_favorite.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_flower.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_game.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_groups.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_home.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_light.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_like.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_love.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_mask.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_money.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_note.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_palette.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_party.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_private.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_setup.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_sport.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_study.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_trade.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_travel.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_unmuted.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_unread.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/filter_work.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/ic_aurora_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/ic_aurora_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/ic_flora_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/ic_flora_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/ic_pluto_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/ic_pluto_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/ic_venus_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/ic_venus_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/ic_vesta_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/ic_vesta_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/icon_6_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/icon_7_launcher_background.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/icon_9_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/icon_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/icon_octo_monet_background.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/icon_yuki_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/intro_octo.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/menu_bookmarks_cn.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/menu_broadcast_cn.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/menu_calls_cn.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/menu_contacts_cn.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/menu_groups_cn.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/menu_invite_cn.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/menu_nearby_cn.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/menu_secret_cn.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/menu_settings_cn.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/msg_edited.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/msg_forward_quote.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/notification.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/octo_notification.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/outline_science_white.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/outline_source_white_28.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/round_auto_fix_high_black.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/round_bedtime_black.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/round_hdr_on_black.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/round_info_white.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/round_landscape_black.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/round_photo_camera_black.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/round_update_white_28.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/settings_appearance.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/video_quality4.png"
    "./TMessagesProj/src/main/res/drawable-mdpi/video_quality5.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/bug_report.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/datacenter_status.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/edit_passcode.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/ev_minus.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/ev_plus.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_airplane.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_all.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_book.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_bot.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_cat.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_channel.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_crown.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_custom.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_favorite.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_flower.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_game.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_groups.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_home.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_light.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_like.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_love.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_mask.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_money.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_note.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_palette.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_party.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_private.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_setup.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_sport.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_study.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_trade.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_travel.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_unmuted.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_unread.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/filter_work.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/ic_aurora_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/ic_aurora_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/ic_flora_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/ic_flora_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/ic_pluto_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/ic_pluto_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/ic_venus_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/ic_venus_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/ic_vesta_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/ic_vesta_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/icon_6_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/icon_7_launcher_background.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/icon_9_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/icon_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/icon_octo_monet_background.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/icon_yuki_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/intro_octo.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/menu_bookmarks_cn.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/menu_broadcast_cn.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/menu_calls_cn.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/menu_contacts_cn.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/menu_groups_cn.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/menu_invite_cn.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/menu_nearby_cn.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/menu_secret_cn.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/menu_settings_cn.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/msg_edited.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/msg_forward_quote.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/notification.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/octo_notification.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/outline_science_white.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/outline_source_white_28.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/round_auto_fix_high_black.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/round_bedtime_black.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/round_hdr_on_black.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/round_info_white.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/round_landscape_black.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/round_photo_camera_black.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/round_update_white_28.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/settings_appearance.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/video_quality4.png"
    "./TMessagesProj/src/main/res/drawable-xhdpi/video_quality5.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/bug_report.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/datacenter_status.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/edit_passcode.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/ev_minus.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/ev_plus.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_airplane.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_all.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_book.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_bot.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_cat.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_channel.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_crown.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_custom.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_favorite.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_flower.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_game.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_groups.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_home.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_light.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_like.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_love.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_mask.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_money.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_note.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_palette.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_party.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_private.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_setup.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_sport.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_study.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_trade.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_travel.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_unmuted.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_unread.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/filter_work.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/ic_aurora_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/ic_aurora_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/ic_flora_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/ic_flora_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/ic_pluto_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/ic_pluto_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/ic_venus_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/ic_venus_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/ic_vesta_datacenter.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/ic_vesta_datacenter_little.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/icon_6_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/icon_7_launcher_background.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/icon_9_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/icon_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/icon_octo_monet_background.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/icon_yuki_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/intro_octo.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/menu_bookmarks_cn.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/menu_broadcast_cn.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/menu_calls_cn.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/menu_contacts_cn.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/menu_groups_cn.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/menu_invite_cn.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/menu_nearby_cn.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/menu_secret_cn.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/menu_settings_cn.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/msg_forward_quote.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/notification.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/octo_notification.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/outline_science_white.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/outline_source_white_28.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/round_auto_fix_high_black.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/round_bedtime_black.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/round_hdr_on_black.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/round_info_white.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/round_landscape_black.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/round_photo_camera_black.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/round_update_white_28.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/settings_appearance.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/video_quality4.png"
    "./TMessagesProj/src/main/res/drawable-xxhdpi/video_quality5.png"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/icon_6_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/icon_7_launcher_background.png"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/icon_9_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/icon_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/icon_octo_monet_background.png"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/icon_yuki_background_sa.png"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/intro_octo.png"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/msg_secret_hw.png"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/octo_notification.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/ic_dev_icon_background.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/ic_dev_icon_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/ic_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/ic_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_2_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_2_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_4_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_4_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_5_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_5_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_5_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_5_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_5_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_6_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_6_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_6_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_6_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_6_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_7_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_7_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_9_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_9_launcher_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_9_launcher_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_9_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_9_launcher_round_sa.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_yuki_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_yuki_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_yuki_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_yuki_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_yuki_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/icon_yuki_launcher_sa.png"
    "./TMessagesProj/src/main/res/mipmap-hdpi/msg_secret_hw.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/ic_dev_icon_background.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/ic_dev_icon_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/ic_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/ic_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_2_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_2_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_4_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_4_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_5_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_5_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_5_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_5_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_5_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_6_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_6_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_6_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_6_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_6_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_7_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_7_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_9_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_9_launcher_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_9_launcher_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_9_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_9_launcher_round_sa.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_yuki_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_yuki_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_yuki_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_yuki_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_yuki_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/icon_yuki_launcher_sa.png"
    "./TMessagesProj/src/main/res/mipmap-mdpi/msg_secret_hw.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/ic_dev_icon_background.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/ic_dev_icon_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/ic_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/ic_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_2_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_2_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_4_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_4_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_5_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_5_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_5_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_5_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_5_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_6_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_6_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_6_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_6_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_6_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_7_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_7_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_9_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_9_launcher_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_9_launcher_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_9_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_9_launcher_round_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_yuki_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_yuki_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_yuki_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_yuki_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_yuki_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/icon_yuki_launcher_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/msg_secret_hw.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/ic_dev_icon_background.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/ic_dev_icon_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/ic_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/ic_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_2_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_2_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_4_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_4_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_5_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_5_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_5_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_5_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_5_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_6_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_6_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_6_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_6_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_6_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_7_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_7_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_9_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_9_launcher_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_9_launcher_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_9_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_9_launcher_round_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_yuki_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_yuki_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_yuki_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_yuki_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_yuki_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/icon_yuki_launcher_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/ic_dev_icon_background.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/ic_dev_icon_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/ic_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_2_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_2_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_3_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_3_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_4_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_4_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_5_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_5_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_5_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_5_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_5_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_6_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_6_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_6_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_6_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_6_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_7_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_7_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_9_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_9_launcher_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_9_launcher_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_9_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_9_launcher_round_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_yuki_foreground.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_yuki_foreground_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_yuki_foreground_sa.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_yuki_launcher.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_yuki_launcher_round.png"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/icon_yuki_launcher_sa.png"
)

webp_files=(
    "./TMessagesProj/src/main/res/drawable/sticker.webp"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/ic_octo_confetti.webp"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/ic_octo_confetti_background.webp"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/ic_octo_confetti_foreground.webp"
    "./TMessagesProj/src/main/res/mipmap-xxxhdpi/ic_octo_confetti_round.webp"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/ic_octo_confetti.webp"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/ic_octo_confetti_background.webp"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/ic_octo_confetti_foreground.webp"
    "./TMessagesProj/src/main/res/mipmap-xxhdpi/ic_octo_confetti_round.webp"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/ic_octo_confetti.webp"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/ic_octo_confetti_background.webp"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/ic_octo_confetti_foreground.webp"
    "./TMessagesProj/src/main/res/mipmap-xhdpi/ic_octo_confetti_round.webp"
    "./TMessagesProj/src/main/res/mipmap-mdpi/ic_octo_confetti.webp"
    "./TMessagesProj/src/main/res/mipmap-mdpi/ic_octo_confetti_background.webp"
    "./TMessagesProj/src/main/res/mipmap-mdpi/ic_octo_confetti_foreground.webp"
    "./TMessagesProj/src/main/res/mipmap-mdpi/ic_octo_confetti_round.webp"
    "./TMessagesProj/src/main/res/mipmap-hdpi/ic_octo_confetti.webp"
    "./TMessagesProj/src/main/res/mipmap-hdpi/ic_octo_confetti_background.webp"
    "./TMessagesProj/src/main/res/mipmap-hdpi/ic_octo_confetti_foreground.webp"
    "./TMessagesProj/src/main/res/mipmap-hdpi/ic_octo_confetti_round.webp"
)

vector_drawable_files=(
    "./TMessagesProj/src/main/res/drawable/account_circle_24px.xml"
    "./TMessagesProj/src/main/res/drawable/account_circle_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/add_a_photo_24px.xml"
    "./TMessagesProj/src/main/res/drawable/add_home_24px.xml"
    "./TMessagesProj/src/main/res/drawable/add_link_24px.xml"
    "./TMessagesProj/src/main/res/drawable/add_reaction_24px.xml"
    "./TMessagesProj/src/main/res/drawable/ar_stickers_24px.xml"
    "./TMessagesProj/src/main/res/drawable/archive_24px.xml"
    "./TMessagesProj/src/main/res/drawable/archive_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/arrow_drop_down_24px.xml"
    "./TMessagesProj/src/main/res/drawable/assignment_24px.xml"
    "./TMessagesProj/src/main/res/drawable/assistant_direction_24px.xml"
    "./TMessagesProj/src/main/res/drawable/attach_file_24px.xml"
    "./TMessagesProj/src/main/res/drawable/auto_delete_24px.xml"
    "./TMessagesProj/src/main/res/drawable/avg_pace_24px.xml"
    "./TMessagesProj/src/main/res/drawable/backspace_24px.xml"
    "./TMessagesProj/src/main/res/drawable/backspace_solid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/bar_chart_off_24px.xml"
    "./TMessagesProj/src/main/res/drawable/baseline_device_android_x_24.xml"
    "./TMessagesProj/src/main/res/drawable/baseline_octo_24.xml"
    "./TMessagesProj/src/main/res/drawable/battery_charging_90_24px.xml"
    "./TMessagesProj/src/main/res/drawable/block_24px.xml"
    "./TMessagesProj/src/main/res/drawable/bluetooth_24px.xml"
    "./TMessagesProj/src/main/res/drawable/blur_on_24px.xml"
    "./TMessagesProj/src/main/res/drawable/bokeh_mode.xml"
    "./TMessagesProj/src/main/res/drawable/bolt_24px.xml"
    "./TMessagesProj/src/main/res/drawable/bookmark_24px.xml"
    "./TMessagesProj/src/main/res/drawable/bookmark_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/brightness_high_24px.xml"
    "./TMessagesProj/src/main/res/drawable/brightness_low_24px.xml"
    "./TMessagesProj/src/main/res/drawable/brush_24px.xml"
    "./TMessagesProj/src/main/res/drawable/calendar_clock_24px.xml"
    "./TMessagesProj/src/main/res/drawable/calendar_month_24px.xml"
    "./TMessagesProj/src/main/res/drawable/call_custom_notification_icon.xml"
    "./TMessagesProj/src/main/res/drawable/call_end_24px.xml"
    "./TMessagesProj/src/main/res/drawable/call_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/campaign_24px.xml"
    "./TMessagesProj/src/main/res/drawable/cancel_24px.xml"
    "./TMessagesProj/src/main/res/drawable/cast_24px.xml"
    "./TMessagesProj/src/main/res/drawable/cast_pause_24px.xml"
    "./TMessagesProj/src/main/res/drawable/chat_24px.xml"
    "./TMessagesProj/src/main/res/drawable/chat_add_on_24px.xml"
    "./TMessagesProj/src/main/res/drawable/chat_paste_go_24px.xml"
    "./TMessagesProj/src/main/res/drawable/chat_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/check_circle_24px.xml"
    "./TMessagesProj/src/main/res/drawable/clear_all_24px.xml"
    "./TMessagesProj/src/main/res/drawable/colorize_24px.xml"
    "./TMessagesProj/src/main/res/drawable/comedy_mask_24px.xml"
    "./TMessagesProj/src/main/res/drawable/content_copy_24px.xml"
    "./TMessagesProj/src/main/res/drawable/content_copy_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/credit_card_24px.xml"
    "./TMessagesProj/src/main/res/drawable/currency_bitcoin_24px.xml"
    "./TMessagesProj/src/main/res/drawable/dark_mode_24px.xml"
    "./TMessagesProj/src/main/res/drawable/database_24px.xml"
    "./TMessagesProj/src/main/res/drawable/delete_24px.xml"
    "./TMessagesProj/src/main/res/drawable/device_octogram.xml"
    "./TMessagesProj/src/main/res/drawable/devices_24px.xml"
    "./TMessagesProj/src/main/res/drawable/do_not_disturb_on_24px.xml"
    "./TMessagesProj/src/main/res/drawable/domino_mask_24px.xml"
    "./TMessagesProj/src/main/res/drawable/download_24px.xml"
    "./TMessagesProj/src/main/res/drawable/draft_24px.xml"
    "./TMessagesProj/src/main/res/drawable/draw_24px.xml"
    "./TMessagesProj/src/main/res/drawable/drive_file_move_24px.xml"
    "./TMessagesProj/src/main/res/drawable/earthquake_24px.xml"
    "./TMessagesProj/src/main/res/drawable/edit_24px.xml"
    "./TMessagesProj/src/main/res/drawable/edit_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/emoji_objects_24px.xml"
    "./TMessagesProj/src/main/res/drawable/emoji_symbols_24px.xml"
    "./TMessagesProj/src/main/res/drawable/error_24px.xml"
    "./TMessagesProj/src/main/res/drawable/face_retouch.xml"
    "./TMessagesProj/src/main/res/drawable/favorite_24px.xml"
    "./TMessagesProj/src/main/res/drawable/favorite_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/fingerprint_24px.xml"
    "./TMessagesProj/src/main/res/drawable/flag_24px.xml"
    "./TMessagesProj/src/main/res/drawable/flash_auto_24px.xml"
    "./TMessagesProj/src/main/res/drawable/flash_off_24px.xml"
    "./TMessagesProj/src/main/res/drawable/flash_on_24px.xml"
    "./TMessagesProj/src/main/res/drawable/flashlight_on_24px.xml"
    "./TMessagesProj/src/main/res/drawable/flight_takeoff_24px.xml"
    "./TMessagesProj/src/main/res/drawable/flip_24px.xml"
    "./TMessagesProj/src/main/res/drawable/flip_camera_android_24px.xml"
    "./TMessagesProj/src/main/res/drawable/folder_delete_24px.xml"
    "./TMessagesProj/src/main/res/drawable/folder_open_24px.xml"
    "./TMessagesProj/src/main/res/drawable/folder_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/forum_24px.xml"
    "./TMessagesProj/src/main/res/drawable/forward_24px.xml"
    "./TMessagesProj/src/main/res/drawable/gif_box_24px.xml"
    "./TMessagesProj/src/main/res/drawable/grid_view_24px.xml"
    "./TMessagesProj/src/main/res/drawable/grid_view_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/group_24px.xml"
    "./TMessagesProj/src/main/res/drawable/group_add_24px.xml"
    "./TMessagesProj/src/main/res/drawable/group_remove_24px.xml"
    "./TMessagesProj/src/main/res/drawable/groups_24px.xml"
    "./TMessagesProj/src/main/res/drawable/help_24px.xml"
    "./TMessagesProj/src/main/res/drawable/high_quality_24px.xml"
    "./TMessagesProj/src/main/res/drawable/history_24px.xml"
    "./TMessagesProj/src/main/res/drawable/home_24px.xml"
    "./TMessagesProj/src/main/res/drawable/i18_up_rating_24px.xml"
    "./TMessagesProj/src/main/res/drawable/ic_launcher_background.xml"
    "./TMessagesProj/src/main/res/drawable/ic_launcher_foreground.xml"
    "./TMessagesProj/src/main/res/drawable/ic_octo.xml"
    "./TMessagesProj/src/main/res/drawable/ic_round_swap_horiz_24.xml"
    "./TMessagesProj/src/main/res/drawable/icon_2_background.xml"
    "./TMessagesProj/src/main/res/drawable/icon_2_background_round.xml"
    "./TMessagesProj/src/main/res/drawable/icon_3_background.xml"
    "./TMessagesProj/src/main/res/drawable/icon_3_background_round.xml"
    "./TMessagesProj/src/main/res/drawable/icon_4_background.xml"
    "./TMessagesProj/src/main/res/drawable/icon_4_background_round.xml"
    "./TMessagesProj/src/main/res/drawable/icon_5_background.xml"
    "./TMessagesProj/src/main/res/drawable/icon_5_background_round.xml"
    "./TMessagesProj/src/main/res/drawable/icon_6_background.xml"
    "./TMessagesProj/src/main/res/drawable/icon_6_background_round.xml"
    "./TMessagesProj/src/main/res/drawable/icon_7_launcher_foreground.xml"
    "./TMessagesProj/src/main/res/drawable/icon_dev_grid_foreground.xml"
    "./TMessagesProj/src/main/res/drawable/icon_octo_monet.xml"
    "./TMessagesProj/src/main/res/drawable/imagesearch_roller_24px.xml"
    "./TMessagesProj/src/main/res/drawable/imagesmode_24px.xml"
    "./TMessagesProj/src/main/res/drawable/info_24px.xml"
    "./TMessagesProj/src/main/res/drawable/keep_24px.xml"
    "./TMessagesProj/src/main/res/drawable/keep_off_24px.xml"
    "./TMessagesProj/src/main/res/drawable/keep_off_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/keep_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/key_24px.xml"
    "./TMessagesProj/src/main/res/drawable/keyboard_24px.xml"
    "./TMessagesProj/src/main/res/drawable/language_24px.xml"
    "./TMessagesProj/src/main/res/drawable/layers_24px.xml"
    "./TMessagesProj/src/main/res/drawable/leaderboard_24px.xml"
    "./TMessagesProj/src/main/res/drawable/library_add_24px.xml"
    "./TMessagesProj/src/main/res/drawable/lightbulb_24px.xml"
    "./TMessagesProj/src/main/res/drawable/line_curve_24px.xml"
    "./TMessagesProj/src/main/res/drawable/link_24px.xml"
    "./TMessagesProj/src/main/res/drawable/link_off_24px.xml"
    "./TMessagesProj/src/main/res/drawable/list_24px.xml"
    "./TMessagesProj/src/main/res/drawable/local_fire_department_24px.xml"
    "./TMessagesProj/src/main/res/drawable/local_shipping_24px.xml"
    "./TMessagesProj/src/main/res/drawable/location_on_24px.xml"
    "./TMessagesProj/src/main/res/drawable/lock_24px.xml"
    "./TMessagesProj/src/main/res/drawable/lock_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/logout_24px.xml"
    "./TMessagesProj/src/main/res/drawable/lunch_dining_24px.xml"
    "./TMessagesProj/src/main/res/drawable/m3_round_bluetooth_audio_24.xml"
    "./TMessagesProj/src/main/res/drawable/m3_round_call_24.xml"
    "./TMessagesProj/src/main/res/drawable/m3_round_call_end_24.xml"
    "./TMessagesProj/src/main/res/drawable/m3_round_flip_camera_ios_24.xml"
    "./TMessagesProj/src/main/res/drawable/m3_round_headphones_24.xml"
    "./TMessagesProj/src/main/res/drawable/m3_round_insert_drive_file_24.xml"
    "./TMessagesProj/src/main/res/drawable/m3_round_keyboard_arrow_down_24.xml"
    "./TMessagesProj/src/main/res/drawable/m3_round_location_on_24.xml"
    "./TMessagesProj/src/main/res/drawable/m3_round_mic_24.xml"
    "./TMessagesProj/src/main/res/drawable/m3_round_mic_off_24.xml"
    "./TMessagesProj/src/main/res/drawable/m3_round_mobile_screen_share_24.xml"
    "./TMessagesProj/src/main/res/drawable/m3_round_send_24.xml"
    "./TMessagesProj/src/main/res/drawable/m3_round_videocam_off_24.xml"
    "./TMessagesProj/src/main/res/drawable/m3_round_volume_up_24.xml"
    "./TMessagesProj/src/main/res/drawable/mail_24px.xml"
    "./TMessagesProj/src/main/res/drawable/map_24px.xml"
    "./TMessagesProj/src/main/res/drawable/mark_chat_read_24px.xml"
    "./TMessagesProj/src/main/res/drawable/mark_chat_unread_24px.xml"
    "./TMessagesProj/src/main/res/drawable/menu_book_24px.xml"
    "./TMessagesProj/src/main/res/drawable/mic_24px.xml"
    "./TMessagesProj/src/main/res/drawable/msg_new_filter.xml"
    "./TMessagesProj/src/main/res/drawable/music_note_24px.xml"
    "./TMessagesProj/src/main/res/drawable/music_off_24px.xml"
    "./TMessagesProj/src/main/res/drawable/my_location_24px.xml"
    "./TMessagesProj/src/main/res/drawable/nearby_24px.xml"
    "./TMessagesProj/src/main/res/drawable/nearby_off_24px.xml"
    "./TMessagesProj/src/main/res/drawable/no_sound_24px.xml"
    "./TMessagesProj/src/main/res/drawable/noise_control_off_24px.xml"
    "./TMessagesProj/src/main/res/drawable/noise_control_on_24px.xml"
    "./TMessagesProj/src/main/res/drawable/notifications_24px.xml"
    "./TMessagesProj/src/main/res/drawable/notifications_off_24px.xml"
    "./TMessagesProj/src/main/res/drawable/notifications_off_solid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/notifications_paused_24px.xml"
    "./TMessagesProj/src/main/res/drawable/notifications_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/octo_108.xml"
    "./TMessagesProj/src/main/res/drawable/octo_splash_320.xml"
    "./TMessagesProj/src/main/res/drawable/octo_splash_320_v2.xml"
    "./TMessagesProj/src/main/res/drawable/open_in_new_24px.xml"
    "./TMessagesProj/src/main/res/drawable/page_info_24px.xml"
    "./TMessagesProj/src/main/res/drawable/palette_24px.xml"
    "./TMessagesProj/src/main/res/drawable/password_24px.xml"
    "./TMessagesProj/src/main/res/drawable/person_24px.xml"
    "./TMessagesProj/src/main/res/drawable/person_add_24px.xml"
    "./TMessagesProj/src/main/res/drawable/person_check_24px.xml"
    "./TMessagesProj/src/main/res/drawable/person_remove_24px.xml"
    "./TMessagesProj/src/main/res/drawable/person_search_24px.xml"
    "./TMessagesProj/src/main/res/drawable/pets_24px.xml"
    "./TMessagesProj/src/main/res/drawable/phone_in_talk_24px.xml"
    "./TMessagesProj/src/main/res/drawable/photo_album_24px.xml"
    "./TMessagesProj/src/main/res/drawable/photo_camera_24px.xml"
    "./TMessagesProj/src/main/res/drawable/photo_library_24px.xml"
    "./TMessagesProj/src/main/res/drawable/picture_in_picture_24px.xml"
    "./TMessagesProj/src/main/res/drawable/pie_chart_24px.xml"
    "./TMessagesProj/src/main/res/drawable/pill_24px.xml"
    "./TMessagesProj/src/main/res/drawable/pip_24px.xml"
    "./TMessagesProj/src/main/res/drawable/pip_exit_24px.xml"
    "./TMessagesProj/src/main/res/drawable/play_circle_24px.xml"
    "./TMessagesProj/src/main/res/drawable/play_circle_solid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/policy_24px.xml"
    "./TMessagesProj/src/main/res/drawable/policy_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/privacy_tip_24px.xml"
    "./TMessagesProj/src/main/res/drawable/qr_code_24px.xml"
    "./TMessagesProj/src/main/res/drawable/query_stats_24px.xml"
    "./TMessagesProj/src/main/res/drawable/quickreply_24px.xml"
    "./TMessagesProj/src/main/res/drawable/record_voice_over_24px.xml"
    "./TMessagesProj/src/main/res/drawable/redeem_24px.xml"
    "./TMessagesProj/src/main/res/drawable/reorder_24px.xml"
    "./TMessagesProj/src/main/res/drawable/reply_24px.xml"
    "./TMessagesProj/src/main/res/drawable/res_003.xml"
    "./TMessagesProj/src/main/res/drawable/res_005.xml"
    "./TMessagesProj/src/main/res/drawable/res_018.xml"
    "./TMessagesProj/src/main/res/drawable/res_030.xml"
    "./TMessagesProj/src/main/res/drawable/res_040.xml"
    "./TMessagesProj/src/main/res/drawable/res_060.xml"
    "./TMessagesProj/src/main/res/drawable/res_064.xml"
    "./TMessagesProj/src/main/res/drawable/res_065.xml"
    "./TMessagesProj/src/main/res/drawable/res_075.xml"
    "./TMessagesProj/src/main/res/drawable/res_081.xml"
    "./TMessagesProj/src/main/res/drawable/res_091.xml"
    "./TMessagesProj/src/main/res/drawable/res_094.xml"
    "./TMessagesProj/src/main/res/drawable/res_096.xml"
    "./TMessagesProj/src/main/res/drawable/res_097.xml"
    "./TMessagesProj/src/main/res/drawable/res_128.xml"
    "./TMessagesProj/src/main/res/drawable/res_129.xml"
    "./TMessagesProj/src/main/res/drawable/res_130.xml"
    "./TMessagesProj/src/main/res/drawable/res_205.xml"
    "./TMessagesProj/src/main/res/drawable/res_216.xml"
    "./TMessagesProj/src/main/res/drawable/res_236.xml"
    "./TMessagesProj/src/main/res/drawable/res_237.xml"
    "./TMessagesProj/src/main/res/drawable/res_258.xml"
    "./TMessagesProj/src/main/res/drawable/res_287.xml"
    "./TMessagesProj/src/main/res/drawable/res_291.xml"
    "./TMessagesProj/src/main/res/drawable/res_302.xml"
    "./TMessagesProj/src/main/res/drawable/res_321.xml"
    "./TMessagesProj/src/main/res/drawable/res_327.xml"
    "./TMessagesProj/src/main/res/drawable/res_378.xml"
    "./TMessagesProj/src/main/res/drawable/reset_settings_24px.xml"
    "./TMessagesProj/src/main/res/drawable/rotate_left_24px.xml"
    "./TMessagesProj/src/main/res/drawable/satellite_alt_24px.xml"
    "./TMessagesProj/src/main/res/drawable/schedule_24px.xml"
    "./TMessagesProj/src/main/res/drawable/schedule_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/school_24px.xml"
    "./TMessagesProj/src/main/res/drawable/sd_card_24px.xml"
    "./TMessagesProj/src/main/res/drawable/search_24px.xml"
    "./TMessagesProj/src/main/res/drawable/sell_24px.xml"
    "./TMessagesProj/src/main/res/drawable/send_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/sentiment_satisfied_24px.xml"
    "./TMessagesProj/src/main/res/drawable/settings_24px.xml"
    "./TMessagesProj/src/main/res/drawable/share_24px.xml"
    "./TMessagesProj/src/main/res/drawable/share_windows_24px.xml"
    "./TMessagesProj/src/main/res/drawable/shield_person_24px.xml"
    "./TMessagesProj/src/main/res/drawable/skull_24px.xml"
    "./TMessagesProj/src/main/res/drawable/smart_toy_24px.xml"
    "./TMessagesProj/src/main/res/drawable/smart_toy_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/solar_arrow_more.xml"
    "./TMessagesProj/src/main/res/drawable/solar_attach_send.xml"
    "./TMessagesProj/src/main/res/drawable/solar_bot_file.xml"
    "./TMessagesProj/src/main/res/drawable/solar_bot_location.xml"
    "./TMessagesProj/src/main/res/drawable/solar_calls_bluetooth.xml"
    "./TMessagesProj/src/main/res/drawable/solar_calls_camera_mini.xml"
    "./TMessagesProj/src/main/res/drawable/solar_calls_decline.xml"
    "./TMessagesProj/src/main/res/drawable/solar_calls_flip.xml"
    "./TMessagesProj/src/main/res/drawable/solar_calls_headphones.xml"
    "./TMessagesProj/src/main/res/drawable/solar_calls_menu_phone.xml"
    "./TMessagesProj/src/main/res/drawable/solar_calls_menu_speaker.xml"
    "./TMessagesProj/src/main/res/drawable/solar_calls_mute_mini.xml"
    "./TMessagesProj/src/main/res/drawable/solar_calls_sharescreen.xml"
    "./TMessagesProj/src/main/res/drawable/solar_calls_unmute.xml"
    "./TMessagesProj/src/main/res/drawable/solar_calls_video.xml"
    "./TMessagesProj/src/main/res/drawable/solar_camera_revert1.xml"
    "./TMessagesProj/src/main/res/drawable/solar_camera_revert2.xml"
    "./TMessagesProj/src/main/res/drawable/solar_chat_calls_voice.xml"
    "./TMessagesProj/src/main/res/drawable/solar_chats_archive.xml"
    "./TMessagesProj/src/main/res/drawable/solar_chats_pin.xml"
    "./TMessagesProj/src/main/res/drawable/solar_chats_replies.xml"
    "./TMessagesProj/src/main/res/drawable/solar_chats_saved.xml"
    "./TMessagesProj/src/main/res/drawable/solar_chats_unpin.xml"
    "./TMessagesProj/src/main/res/drawable/solar_emoji_tabs_faves.xml"
    "./TMessagesProj/src/main/res/drawable/solar_emoji_tabs_new1.xml"
    "./TMessagesProj/src/main/res/drawable/solar_emoji_tabs_new2.xml"
    "./TMessagesProj/src/main/res/drawable/solar_emoji_tabs_new3.xml"
    "./TMessagesProj/src/main/res/drawable/solar_files_folder.xml"
    "./TMessagesProj/src/main/res/drawable/solar_files_gallery.xml"
    "./TMessagesProj/src/main/res/drawable/solar_files_internal.xml"
    "./TMessagesProj/src/main/res/drawable/solar_files_storage.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filled_fire.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_book.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_bot.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_cat.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_custom.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_favorite.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_flower.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_game.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_home.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_light.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_like.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_love.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_mask.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_money.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_note.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_palette.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_party.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_private.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_setup.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_sport.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_study.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_trade.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_travel.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_unmuted.xml"
    "./TMessagesProj/src/main/res/drawable/solar_filter_work.xml"
    "./TMessagesProj/src/main/res/drawable/solar_fingerprint.xml"
    "./TMessagesProj/src/main/res/drawable/solar_flash_auto.xml"
    "./TMessagesProj/src/main/res/drawable/solar_flash_off.xml"
    "./TMessagesProj/src/main/res/drawable/solar_flash_on.xml"
    "./TMessagesProj/src/main/res/drawable/solar_ghost.xml"
    "./TMessagesProj/src/main/res/drawable/solar_group_edit.xml"
    "./TMessagesProj/src/main/res/drawable/solar_ic_ab_search.xml"
    "./TMessagesProj/src/main/res/drawable/solar_ic_arrow_drop_down.xml"
    "./TMessagesProj/src/main/res/drawable/solar_ic_chatlist_add_2.xml"
    "./TMessagesProj/src/main/res/drawable/solar_ic_gallery_background.xml"
    "./TMessagesProj/src/main/res/drawable/solar_ic_goinline.xml"
    "./TMessagesProj/src/main/res/drawable/solar_ic_lock_header.xml"
    "./TMessagesProj/src/main/res/drawable/solar_ic_masks_msk1.xml"
    "./TMessagesProj/src/main/res/drawable/solar_ic_outinline.xml"
    "./TMessagesProj/src/main/res/drawable/solar_ic_send.xml"
    "./TMessagesProj/src/main/res/drawable/solar_input_attach.xml"
    "./TMessagesProj/src/main/res/drawable/solar_input_bot1.xml"
    "./TMessagesProj/src/main/res/drawable/solar_input_bot2.xml"
    "./TMessagesProj/src/main/res/drawable/solar_input_calendar1.xml"
    "./TMessagesProj/src/main/res/drawable/solar_input_calendar2.xml"
    "./TMessagesProj/src/main/res/drawable/solar_input_forward.xml"
    "./TMessagesProj/src/main/res/drawable/solar_input_keyboard.xml"
    "./TMessagesProj/src/main/res/drawable/solar_input_mic.xml"
    "./TMessagesProj/src/main/res/drawable/solar_input_notify_off.xml"
    "./TMessagesProj/src/main/res/drawable/solar_input_notify_on.xml"
    "./TMessagesProj/src/main/res/drawable/solar_input_reply.xml"
    "./TMessagesProj/src/main/res/drawable/solar_input_schedule.xml"
    "./TMessagesProj/src/main/res/drawable/solar_input_smile.xml"
    "./TMessagesProj/src/main/res/drawable/solar_input_video.xml"
    "./TMessagesProj/src/main/res/drawable/solar_input_video_pressed.xml"
    "./TMessagesProj/src/main/res/drawable/solar_instant_camera.xml"
    "./TMessagesProj/src/main/res/drawable/solar_left_status_profile.xml"
    "./TMessagesProj/src/main/res/drawable/solar_list_mute.xml"
    "./TMessagesProj/src/main/res/drawable/solar_list_pin.xml"
    "./TMessagesProj/src/main/res/drawable/solar_media_flip.xml"
    "./TMessagesProj/src/main/res/drawable/solar_media_like.xml"
    "./TMessagesProj/src/main/res/drawable/solar_media_settings.xml"
    "./TMessagesProj/src/main/res/drawable/solar_media_share.xml"
    "./TMessagesProj/src/main/res/drawable/solar_menu_devices.xml"
    "./TMessagesProj/src/main/res/drawable/solar_menu_gift.xml"
    "./TMessagesProj/src/main/res/drawable/solar_menu_link_create.xml"
    "./TMessagesProj/src/main/res/drawable/solar_menu_nearby_off.xml"
    "./TMessagesProj/src/main/res/drawable/solar_menu_reply.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_animations.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_archived_stickers.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_ask_question.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_autodelete.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_battery.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_block2.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_call_earpiece.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_chats_add.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_data.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_discussion.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_email.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_folder.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_gif.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_help.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_language.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_night_auto.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_permissions.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_policy.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_proxy_off.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_proxy_on.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_secret.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_sticker.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_trending.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg2_videocall.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_addbio.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_addcontact.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_addfolder.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_addphoto.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_admin_add.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_admins.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_allowspeak.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_autodelete_1d.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_autodelete_1m.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_autodelete_1w.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_autodelete_badge2.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_background.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_block.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_bot.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_brightness_high.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_brightness_low.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_calendar.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_calendar2.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_calls_14.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_calls_hw.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_calls_ny.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_calls_regular.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_camera.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_cancel.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_channel.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_chats_remove.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_clear.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_clear_input.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_clear_recent.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_clearcache.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_colors.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_contacts.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_contacts_14.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_contacts_hw.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_contacts_name.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_contacts_ny.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_contacts_time.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_copy.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_copy_filled.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_current_location.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_delete_auto.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_download.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_edit.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_emoji_activities.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_emoji_cat.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_emoji_flags.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_emoji_food.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_emoji_objects.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_emoji_other.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_emoji_recent.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_emoji_travel.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_endcall.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_fave.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filehq.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_blocked.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_data_calls.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_data_files.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_data_messages.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_data_music.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_data_photos.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_data_received.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_data_sent.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_data_videos.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_data_voice.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_datausage.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_menu_channels.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_menu_groups.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_menu_users.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_sdcard.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_shareout.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_filled_storageusage.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_folders_archive.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_folders_bots.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_folders_channels.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_folders_groups.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_folders_muted.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_folders_private.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_folders_read.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_folders_requests.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_forward_replace.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_gallery.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_gif_add.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_groups.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_groups_14.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_groups_create.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_groups_hw.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_groups_ny.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_header_draw.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_header_share.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_home.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_hybrid.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_info.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_instant.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_instant_link.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_invited.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_jobtitle.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_leave.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_link_1.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_link_2.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_location.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_location_alert.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_log.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_map.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_map_type.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_markread.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_markunread.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_mask.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_media.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_message.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_mini_autodelete.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_mini_autodelete_empty.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_mini_customize.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_mini_qr.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_msgbubble.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_msgbubble2.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_mute.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_mute_1h.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_mute_period.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_nearby.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_nearby_ny.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_newphone.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_noise_off.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_noise_on.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_notspam.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_online.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_palette.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_payment_card.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_payment_delivery.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_payment_provider.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_photo_blur.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_photo_curve.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_photo_rotate.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_photos.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_pin_code.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_pinnedlist.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_pollstop.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_qr_mini.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_qrcode.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_rate_down.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_rate_up.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_reactions_filled.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_recent.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_remove.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_removefolder.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_replace.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_reply_small.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_report.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_report_drugs.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_report_fake.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_report_personal.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_report_violence.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_report_xxx.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_reset.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_satellite.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_saved.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_saved_14.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_saved_hw.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_saved_ny.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_screencast.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_screencast_off.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_search.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_secret_14.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_secret_hw.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_secret_ny.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_select.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_send.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_sendfile.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_settings.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_settings_14.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_settings_hw.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_settings_ny.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_shareout.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_signed.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_silent.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_speed.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_stats.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_status_edit.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_status_set.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_tone_add.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_tone_off.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_tone_on.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_topic_create.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_topics.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_translate.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_unarchive.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_unfave.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_ungroup.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_unmute.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_unvote.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_user_remove.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_usersearch.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_viewchats.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_viewintopic.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_viewreplies.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_views.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_voice_bluetooth.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_voice_headphones.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_voice_phone.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_voice_pip.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_voicechat.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_voicechat2.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_work.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_zoomin.xml"
    "./TMessagesProj/src/main/res/drawable/solar_msg_zoomout.xml"
    "./TMessagesProj/src/main/res/drawable/solar_navigate.xml"
    "./TMessagesProj/src/main/res/drawable/solar_notifications_mute1h.xml"
    "./TMessagesProj/src/main/res/drawable/solar_notifications_mute2d.xml"
    "./TMessagesProj/src/main/res/drawable/solar_permissions_camera1.xml"
    "./TMessagesProj/src/main/res/drawable/solar_permissions_camera2.xml"
    "./TMessagesProj/src/main/res/drawable/solar_permissions_gallery1.xml"
    "./TMessagesProj/src/main/res/drawable/solar_permissions_gallery2.xml"
    "./TMessagesProj/src/main/res/drawable/solar_photo_paint_brush.xml"
    "./TMessagesProj/src/main/res/drawable/solar_photo_undo.xml"
    "./TMessagesProj/src/main/res/drawable/solar_picker.xml"
    "./TMessagesProj/src/main/res/drawable/solar_profile_discuss.xml"
    "./TMessagesProj/src/main/res/drawable/solar_profile_newmsg.xml"
    "./TMessagesProj/src/main/res/drawable/solar_qr_flashlight.xml"
    "./TMessagesProj/src/main/res/drawable/solar_qr_gallery.xml"
    "./TMessagesProj/src/main/res/drawable/solar_screencast_big.xml"
    "./TMessagesProj/src/main/res/drawable/solar_share_arrow.xml"
    "./TMessagesProj/src/main/res/drawable/solar_smallanimationpin.xml"
    "./TMessagesProj/src/main/res/drawable/solar_smiles_inputsearch.xml"
    "./TMessagesProj/src/main/res/drawable/solar_smiles_tab_settings.xml"
    "./TMessagesProj/src/main/res/drawable/solar_stickers_empty.xml"
    "./TMessagesProj/src/main/res/drawable/solar_stickers_favorites.xml"
    "./TMessagesProj/src/main/res/drawable/solar_stickers_gifs_trending.xml"
    "./TMessagesProj/src/main/res/drawable/solar_tabs_reorder.xml"
    "./TMessagesProj/src/main/res/drawable/solar_theme_picker.xml"
    "./TMessagesProj/src/main/res/drawable/solar_verified_profile.xml"
    "./TMessagesProj/src/main/res/drawable/solar_video_send_mute.xml"
    "./TMessagesProj/src/main/res/drawable/solar_video_send_unmute.xml"
    "./TMessagesProj/src/main/res/drawable/spa_24px.xml"
    "./TMessagesProj/src/main/res/drawable/speed_24px.xml"
    "./TMessagesProj/src/main/res/drawable/sports_esports_24px.xml"
    "./TMessagesProj/src/main/res/drawable/sports_soccer_24px.xml"
    "./TMessagesProj/src/main/res/drawable/star_24px.xml"
    "./TMessagesProj/src/main/res/drawable/star_24px_unsolid.xml"
    "./TMessagesProj/src/main/res/drawable/storage_24px.xml"
    "./TMessagesProj/src/main/res/drawable/sync_alt_24px.xml"
    "./TMessagesProj/src/main/res/drawable/system_camera_icon.xml"
    "./TMessagesProj/src/main/res/drawable/telegram_camera_icon.xml"
    "./TMessagesProj/src/main/res/drawable/thumb_down_24px.xml"
    "./TMessagesProj/src/main/res/drawable/thumb_up_24px.xml"
    "./TMessagesProj/src/main/res/drawable/thumb_up_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/topic_24px.xml"
    "./TMessagesProj/src/main/res/drawable/train_24px.xml"
    "./TMessagesProj/src/main/res/drawable/translate_24px.xml"
    "./TMessagesProj/src/main/res/drawable/trending_up_24px.xml"
    "./TMessagesProj/src/main/res/drawable/tty_24px.xml"
    "./TMessagesProj/src/main/res/drawable/tune_24px.xml"
    "./TMessagesProj/src/main/res/drawable/undo_24px.xml"
    "./TMessagesProj/src/main/res/drawable/ungroup_24px.xml"
    "./TMessagesProj/src/main/res/drawable/upload_24px.xml"
    "./TMessagesProj/src/main/res/drawable/videocam_24px.xml"
    "./TMessagesProj/src/main/res/drawable/videocam_filled_24px.xml"
    "./TMessagesProj/src/main/res/drawable/visibility_24px.xml"
    "./TMessagesProj/src/main/res/drawable/voice_chat.xml"
    "./TMessagesProj/src/main/res/drawable/voice_chat_24px.xml"
    "./TMessagesProj/src/main/res/drawable/voice_chat_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/volume_off_24px.xml"
    "./TMessagesProj/src/main/res/drawable/volume_off_unsolid_24px.xml"
    "./TMessagesProj/src/main/res/drawable/volume_up_24px.xml"
    "./TMessagesProj/src/main/res/drawable/wallpaper_24px.xml"
    "./TMessagesProj/src/main/res/drawable/wine_bar_24px.xml"
    "./TMessagesProj/src/main/res/drawable/work_24px.xml"
    "./TMessagesProj/src/main/res/drawable/x_camera_icon.xml"
    "./TMessagesProj/src/main/res/drawable/zoom_in_24px.xml"
    "./TMessagesProj/src/main/res/drawable/zoom_out_24px.xml"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/ic_dev_icon.xml"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/ic_launcher.xml"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/ic_launcher_round.xml"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/ic_octo_confetti.xml"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/ic_octo_confetti_round.xml"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/icon_7_launcher.xml"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/icon_7_launcher_round.xml"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/icon_9_launcher.xml"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/icon_9_launcher_round.xml"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/icon_octo_monet_launcher.xml"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/icon_octo_monet_round.xml"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/icon_yuki_launcher.xml"
    "./TMessagesProj/src/main/res/drawable-xxxhdpi/icon_yuki_launcher_round.xml"
    "./TMessagesProj/src/main/res/drawable/aifeatures_solar.xml"
    "./TMessagesProj/src/main/res/drawable/chatgpt.xml"
    "./TMessagesProj/src/main/res/drawable/cup_star_solar.xml"
    "./TMessagesProj/src/main/res/drawable/gemini.xml"
    "./TMessagesProj/src/main/res/drawable/kitsugram.xml"
    "./TMessagesProj/src/main/res/drawable/materialgram.xml"
    "./TMessagesProj/src/main/res/drawable/menu_feature_ocr.xml"
    "./TMessagesProj/src/main/res/drawable/openrouter.xml"
    "./TMessagesProj/src/main/res/drawable/settings_octo.xml"
    "./TMessagesProj/src/main/res/drawable/swiftgram.xml"
    "./TMessagesProj/src/main/res/drawable/text_field_focus.xml"
    "./TMessagesProj/src/main/res/drawable/repeat_solar.xml"
)

metadata_files=(
    "${png_files[@]}"
    "${webp_files[@]}"
)

check_tool() {
    local tool_name="$1"
    if ! command -v "$tool_name" &> /dev/null; then
        echo -e "${BOLD_RED}Error:${RESET} Required tool '${BOLD}$tool_name${RESET}' is not installed or not in PATH."
        return 1
    else
        return 0
    fi
}

get_size() {
    local file="$1"
    if [[ "$(uname)" == "Darwin" ]]; then
        stat -f%z "$file" 2>/dev/null || echo "0"
    else
        stat -c%s "$file" 2>/dev/null || echo "0"
    fi
}

compress_png() {
    local files_to_process=("$@")
    local total_files=${#files_to_process[@]}
    echo -e "${BOLD_BLUE}--- Compressing PNG Files ($total_files) ---${RESET}"
    local processed_count=0
    local skipped_count=0
    local failed_count=0
    local total_saved=0

    if [ $total_files -eq 0 ]; then
        echo -e "${YELLOW}No PNG files found to process.${RESET}"
        echo
        return
    fi

    check_tool "pngquant" || return 1
    check_tool "optipng" || return 1
    echo

    for file in "${files_to_process[@]}"; do
        if [ ! -f "$file" ]; then
             echo -e "${YELLOW}[PNG] Skipping (Not Found):${RESET} $file"
             ((skipped_count++))
             continue
        fi

        echo -en "${CYAN}[PNG]${RESET} Processing: $file ..."
        local original_size
        original_size=$(get_size "$file")
        local temp_png_file="${file}.tmp.png"

        local pngquant_success=true
        if ! pngquant --quality=80-98 --speed 1 --skip-if-larger --output "$temp_png_file" -- "$file"; then
            if [ -f "$temp_png_file" ]; then
                rm "$temp_png_file"
            fi
            pngquant_success=false
        fi

        local file_to_optimize="$file"
        if [ "$pngquant_success" = true ] && [ -f "$temp_png_file" ]; then
            file_to_optimize="$temp_png_file"
        fi

        local optipng_success=true
        if ! optipng -o3 -strip all -quiet -- "$file_to_optimize"; then
            echo -e " ${BOLD_RED}optipng failed!${RESET}"
            if [ "$file_to_optimize" = "$temp_png_file" ]; then rm "$file_to_optimize"; fi
            ((failed_count++))
            optipng_success=false
            continue
        fi

        if [ "$pngquant_success" = true ] && [ "$optipng_success" = true ] && [ "$file_to_optimize" = "$temp_png_file" ]; then
             if ! mv "$temp_png_file" "$file"; then
                  echo -e " ${BOLD_RED}Failed to move optimized file!${RESET}"
                  rm "$temp_png_file"
                  ((failed_count++))
                  continue
             fi
        elif [ "$pngquant_success" = false ] && [ "$optipng_success" = false ]; then
             echo -e " ${YELLOW}Failed (both tools)${RESET}"
             ((failed_count++))
             continue
        fi

        local new_size
        new_size=$(get_size "$file")
        local saved=$((original_size - new_size))
        if [ "$saved" -gt 0 ]; then
             total_saved=$((total_saved + saved))
             local percent=0
             if [ "$original_size" -gt 0 ]; then
                  percent=$(( (saved * 100) / original_size ))
             fi
             echo -e " ${GREEN}Done.${RESET} (Saved: ${saved} B / ${percent}%)"
        elif [ "$saved" -eq 0 ]; then
             echo -e " ${YELLOW}Done.${RESET} (No size change)"
        else
             echo -e " ${YELLOW}Done.${RESET} (Size increased? Original: ${original_size}B, New: ${new_size}B)"
        fi
        ((processed_count++))

    done
    echo -e "${BOLD_BLUE}--- PNG Compression Complete ---${RESET}"
    echo -e "Processed: ${GREEN}$processed_count${RESET}, Skipped: ${YELLOW}$skipped_count${RESET}, Failed: ${RED}$failed_count${RESET}. Total Bytes Saved: ${GREEN}${total_saved}${RESET}"
    echo
}

compress_webp() {
    local files_to_process=("$@")
    local total_files=${#files_to_process[@]}
    echo -e "${BOLD_BLUE}--- Compressing WebP Files ($total_files) ---${RESET}"
    local processed_count=0
    local skipped_count=0
    local failed_count=0
    local total_saved=0

    if [ $total_files -eq 0 ]; then
        echo -e "${YELLOW}No WebP files found to process.${RESET}"
        echo
        return
    fi

    check_tool "cwebp" || return 1
    echo

    for file in "${files_to_process[@]}"; do
         if [ ! -f "$file" ]; then
             echo -e "${YELLOW}[WebP] Skipping (Not Found):${RESET} $file"
             ((skipped_count++))
             continue
        fi

        echo -en "${CYAN}[WebP]${RESET} Processing: $file ..."
        local original_size
        original_size=$(get_size "$file")
        local temp_file="${file}.tmp.webp"

        if cwebp -q 80 -m 6 -mt -quiet "$file" -o "$temp_file" 2>/dev/null; then
            local new_size
            new_size=$(get_size "$temp_file")
            if [ "$new_size" -lt "$original_size" ] && [ "$new_size" -gt 0 ]; then
                if ! mv "$temp_file" "$file"; then
                     echo -e " ${BOLD_RED}Failed to replace original file!${RESET}"
                     rm -f "$temp_file"
                     ((failed_count++))
                     continue
                fi
                local saved=$((original_size - new_size))
                total_saved=$((total_saved + saved))
                local percent=0
                if [ "$original_size" -gt 0 ]; then
                    percent=$(( (saved * 100) / original_size ))
                fi
                echo -e " ${GREEN}Done.${RESET} (Saved: ${saved} B / ${percent}%)"
                ((processed_count++))
            else
                echo -e " ${YELLOW}Done.${RESET} (No size reduction, keeping original)"
                rm -f "$temp_file"
                ((processed_count++))
            fi
        else
            echo -e " ${BOLD_RED}cwebp failed!${RESET}"
            rm -f "$temp_file"
            ((failed_count++))
        fi
    done
    echo -e "${BOLD_BLUE}--- WebP Compression Complete ---${RESET}"
    echo -e "Processed: ${GREEN}$processed_count${RESET}, Skipped: ${YELLOW}$skipped_count${RESET}, Failed: ${RED}$failed_count${RESET}. Total Bytes Saved: ${GREEN}${total_saved}${RESET}"
    echo
}

compress_vector_drawables() {
    local files_to_process=("$@")
    local total_files=${#files_to_process[@]}
    echo -e "${BOLD_BLUE}--- Compressing Vector Drawable (XML) Files ($total_files) ---${RESET}"
    local processed_count=0
    local skipped_count=0
    local failed_count=0
    local total_saved=0

    if [ $total_files -eq 0 ]; then
        echo -e "${YELLOW}No Vector Drawable XML files found to process.${RESET}"
        echo
        return
    fi

    check_tool "svgo" || return 1
    echo

    echo -e "${BOLD_YELLOW}WARNING:${RESET} SVGO optimization modifies XML structure."
    echo -e "It's generally safe for Android VectorDrawables, but ${BOLD}review changes${RESET} if you encounter rendering issues."
    local confirm=""
    read -p "$(echo -e "${BOLD_MAGENTA}Proceed with Vector Drawable (XML) compression? (y/N):${RESET} ")" confirm
    if [[ ! "$confirm" =~ ^[Yy]([Ee][Ss])?$ ]]; then
        echo -e "${YELLOW}Skipping Vector Drawable compression.${RESET}"
        echo
        return
    fi
    echo

    for file in "${files_to_process[@]}"; do
        if [ ! -f "$file" ]; then
            echo -e "${YELLOW}[Vector] Skipping (Not Found):${RESET} $file"
            ((skipped_count++))
            continue
        fi

        echo -en "${CYAN}[Vector]${RESET} Processing: $file ..."
        local original_size
        original_size=$(get_size "$file")

        if svgo --multipass --quiet --input "$file" --output "$file" 2>/dev/null; then
            local new_size
            new_size=$(get_size "$file")
            local saved=$((original_size - new_size))

            if [ "$saved" -gt 0 ]; then
                 total_saved=$((total_saved + saved))
                 local percent=0
                 if [ "$original_size" -gt 0 ]; then
                    percent=$(( (saved * 100) / original_size ))
                 fi
                 echo -e " ${GREEN}Done.${RESET} (Saved: ${saved} B / ${percent}%)"
            elif [ "$saved" -eq 0 ]; then
                 echo -e " ${YELLOW}Done.${RESET} (No size change)"
            else
                 echo -e " ${YELLOW}Done.${RESET} (Size increased? Original: ${original_size}B, New: ${new_size}B)"
            fi
            ((processed_count++))
        else
            echo -e " ${BOLD_RED}svgo failed! Check the file.${RESET}"
            ((failed_count++))
        fi
    done
    echo -e "${BOLD_BLUE}--- Vector Drawable Compression Complete ---${RESET}"
    echo -e "Processed: ${GREEN}$processed_count${RESET}, Skipped: ${YELLOW}$skipped_count${RESET}, Failed: ${RED}$failed_count${RESET}. Total Bytes Saved: ${GREEN}${total_saved}${RESET}"
    echo
}

remove_metadata() {
    local files_to_process=("$@")
    local total_files=${#files_to_process[@]}
    echo -e "${BOLD_MAGENTA}--- Removing Metadata (EXIF, etc.) from ($total_files) files ---${RESET}"
    local processed_count=0
    local skipped_type_count=0
    local not_found_count=0
    local issue_count=0

    if [ $total_files -eq 0 ]; then
        echo -e "${YELLOW}No PNG or WebP files specified for metadata removal.${RESET}"
        echo
        return
    fi

    check_tool "exiftool" || return 1
    check_tool "file" || return 1
    echo

    for file in "${files_to_process[@]}"; do
        if [ ! -f "$file" ]; then
            ((not_found_count++))
            continue
        fi

        local file_type
        file_type=$(file -b --mime-type "$file")

        if [[ "$file_type" == "image/png" || "$file_type" == "image/webp" ]]; then
            echo -en "${CYAN}[Meta]${RESET} Stripping: $file ..."
            local error_output
            set +e
            error_output=$(exiftool -fast -q -all= -overwrite_original "$file" 2>&1)
            local exit_status=$?
            set -e

            if [ $exit_status -eq 0 ]; then
                 if [[ "$error_output" == *"0 image files updated"* && "$error_output" == *"1 image files unchanged"* ]]; then
                     echo -e " ${YELLOW}Done (already clean).${RESET}"
                 elif [[ "$error_output" == *"Warning: [minor] Adjusted MakerNotes base"* || "$error_output" == *"Warning: Error opening file"* ]]; then
                      echo -e " ${YELLOW}Done (with minor warnings).${RESET}"
                      ((issue_count++))
                 elif [[ -n "$error_output" ]]; then
                     echo -e " ${YELLOW}Done (with warnings).${RESET}"
                     ((issue_count++))
                 else
                     echo -e " ${GREEN}Done.${RESET}"
                 fi
                 ((processed_count++))
            else
                echo -e " ${BOLD_RED}exiftool failed!${RESET} Exit code: $exit_status"
                if [[ -n "$error_output" ]]; then echo "$error_output"; fi
                ((issue_count++))
            fi

        else
             ((skipped_type_count++))
        fi
    done
    echo -e "${BOLD_MAGENTA}--- Metadata Removal Complete ---${RESET}"
    echo -e "Processed: ${GREEN}$processed_count${RESET}, Not Found: ${YELLOW}$not_found_count${RESET}, Wrong Type: ${YELLOW}$skipped_type_count${RESET}, Issues/Warnings: ${RED}$issue_count${RESET}"
    echo
}

usage() {
    echo -e "${BOLD_CYAN}Usage:${RESET} $0 [options] [TARGET_DIRECTORY]"
    echo -e "  Optimizes Android resource images (PNG, WebP, Vector Drawables) in place."
    echo -e "  Searches within TARGET_DIRECTORY (defaults to './app/src/main/res')."
    echo
    echo -e "${CYAN}Options:${RESET}"
    echo -e "  ${YELLOW}--png${RESET}          Compress PNG files (pngquant + optipng)."
    echo -e "  ${YELLOW}--webp${RESET}         Compress WebP files (cwebp)."
    echo -e "  ${YELLOW}--vector${RESET}       Compress Vector Drawable XML files (svgo)."
    echo -e "  ${YELLOW}--metadata${RESET}     Remove metadata from PNG/WebP files (exiftool)."
    echo -e "  ${YELLOW}--all${RESET}          Run all optimization tasks (PNG, WebP, Vector, Metadata)."
    echo -e "  ${YELLOW}--help, -h${RESET}     Show this help message."
    echo
    echo -e "${CYAN}Example:${RESET}"
    echo -e "  ${BOLD}./optimize_res.sh --all /path/to/my/android/project${RESET}"
    echo -e "  ${BOLD}./optimize_res.sh --png --metadata ./src/main/res${RESET}"
    exit 0
}

DO_PNG=false
DO_WEBP=false
DO_VECTOR=false
DO_METADATA=false
DO_ALL=false
TARGET_DIR=""
declare -a png_files webp_files vector_drawable_files metadata_files_to_process

while [[ "$#" -gt 0 ]]; do
    case $1 in
        --png) DO_PNG=true; shift ;;
        --webp) DO_WEBP=true; shift ;;
        --vector) DO_VECTOR=true; shift ;;
        --metadata) DO_METADATA=true; shift ;;
        --all)
            DO_ALL=true
            DO_PNG=true
            DO_WEBP=true
            DO_VECTOR=true
            DO_METADATA=true
            shift ;;
        --help|-h) usage ;;
        -*) echo -e "${BOLD_RED}Unknown option:${RESET} $1"; echo; usage ;;
        *)
           if [ -z "$TARGET_DIR" ]; then
               TARGET_DIR="$1"
           else
               echo -e "${BOLD_RED}Error:${RESET} Unexpected argument '$1'. Only one target directory is allowed."
               echo; usage
           fi
           shift ;;
    esac
done

if [ -z "$TARGET_DIR" ]; then
    if [ -d "./app/src/main/res" ]; then
        TARGET_DIR="./app/src/main/res"
    elif [ -d "./src/main/res" ]; then
         TARGET_DIR="./src/main/res"
    else
        TARGET_DIR="."
    fi
    echo -e "${YELLOW}No target directory specified, automatically using:${RESET} $TARGET_DIR"
fi

if [ ! -d "$TARGET_DIR" ]; then
    echo -e "${BOLD_RED}Error:${RESET} Target directory '$TARGET_DIR' not found or is not a directory."
    exit 1
fi
TARGET_DIR=$(cd "$TARGET_DIR" && pwd)

if [ "$DO_PNG" = false ] && [ "$DO_WEBP" = false ] && [ "$DO_VECTOR" = false ] && [ "$DO_METADATA" = false ]; then
    echo -e "${BOLD_RED}Error:${RESET} No action specified. Use --png, --webp, --vector, --metadata, or --all."
    echo; usage
fi

echo -e "${BOLD_CYAN}--- Checking Required Tools ---${RESET}"
all_tools_ok=true
if [ "$DO_PNG" = true ]; then
    check_tool "pngquant" || all_tools_ok=false
    check_tool "optipng" || all_tools_ok=false
fi
if [ "$DO_WEBP" = true ]; then
     check_tool "cwebp" || all_tools_ok=false
fi
if [ "$DO_VECTOR" = true ]; then
     check_tool "svgo" || all_tools_ok=false
fi
if [ "$DO_METADATA" = true ]; then
     check_tool "exiftool" || all_tools_ok=false
     check_tool "file" || all_tools_ok=false
fi

if [ "$all_tools_ok" = false ]; then
    echo -e "${BOLD_RED}Error:${RESET} Missing one or more required tools. Please install them and try again."
    exit 1
fi
echo -e "${BOLD_GREEN}All required tools found.${RESET}"
echo

echo -e "${BOLD_CYAN}--- Locating Files in '$TARGET_DIR' ---${RESET}"

if [ "$DO_METADATA" = true ]; then
   metadata_files_to_process+=( "${png_files[@]}" )
   metadata_files_to_process+=( "${webp_files[@]}" )
fi

if [ "$DO_METADATA" = true ]; then
    remove_metadata "${metadata_files_to_process[@]}"
fi

if [ "$DO_PNG" = true ]; then
    compress_png "${png_files[@]}"
fi

if [ "$DO_WEBP" = true ]; then
    compress_webp "${webp_files[@]}"
fi

if [ "$DO_VECTOR" = true ]; then
    compress_vector_drawables "${vector_drawable_files[@]}"
fi

echo -e "${BOLD_GREEN}--- Script Finished ---${RESET}"
exit 0