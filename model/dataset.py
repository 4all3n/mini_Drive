import random
import os

random.seed(42)  

CATEGORIES = {
    "Documents": {
        "stems": [
            "Q1_Financial_Report", "employee_handbook", "Q3_Budget_Forecast",
            "marketing_strategy", "client_meeting_notes", "invoice_10425",
            "tax_return_2025", "vendor_contract_v2", "company_policies",
            "sales_pitch_deck", "expense_report_january", "NDA_signed",
            "resume_harsh", "cover_letter", "annual_review_2026",
            "project_proposal", "board_minutes_sept", "insurance_claim",
            "lease_agreement", "privacy_policy", "terms_of_service",
            "purchase_order_789", "shipping_manifest", "audit_report",
            "compliance_checklist", "risk_assessment", "SOP_document",
            "user_manual_v3", "training_guide", "onboarding_packet",
            "performance_review_q2", "salary_slip_august", "bank_statement",
            "credit_report", "thesis_draft_final", "research_abstract",
            "conference_paper", "grant_application", "lab_notebook_entry",
            "patient_record_anon", "discharge_summary", "prescription_form",
            "medical_certificate", "exam_results_2026", "syllabus_sem3",
            "assignment_submission", "lecture_notes_ch5", "bibliography",
            "case_study_analysis", "survey_responses", "feedback_summary",
            "newsletter_october", "press_release", "memo_internal",
            "agenda_weekly", "transcript_interview", "contract_amendment",
            "warranty_certificate", "product_specification", "datasheet_v2",
        ],
        "extensions": [".pdf", ".docx", ".txt", ".xlsx", ".pptx", ".csv",
                       ".xls", ".ppt", ".odt", ".rtf", ".doc"],
    },

    "Code": {
        "stems": [
            "MainApp", "utils", "index", "style", "package",
            "script", "database_schema", "Dockerfile", "gitignore_template",
            "pom", "application_properties", "auth_controller",
            "header", "build_gradle", "tsconfig",
            "app_module", "FileNetworkService", "docker-compose",
            "README", "server", "router", "middleware",
            "config", "constants", "helpers", "validators",
            "test_auth", "test_upload", "api_handler",
            "webpack_config", "babel_config", "eslint_config",
            "jest_config", "setupTests", "App_test",
            "LoginComponent", "DashboardView", "UserModel",
            "FileRepository", "SecurityConfig", "JwtTokenProvider",
            "AuthService", "FileService", "DatabaseMigration",
            "schema_v2", "seed_data", "rollback_migration",
            "Makefile_template", "CMakeLists", "cargo_toml",
            "go_mod", "requirements_lock", "Pipfile",
            "manage", "wsgi", "asgi",
            "urls", "views", "serializers",
            "forms", "admin_panel", "signals",
            "tasks_celery", "consumers", "routing",
            "proto_definitions", "grpc_server", "graphql_schema",
        ],
        "extensions": [".java", ".py", ".js", ".html", ".css", ".cpp",
                       ".ts", ".go", ".rs", ".sql", ".json", ".xml",
                       ".yml", ".yaml", ".h", ".c", ".rb", ".php",
                       ".swift", ".kt", ".scala", ".lua"],
    },

    "Media": {
        "stems": [
            "profile_picture", "vacation_hawaii", "logo_transparent",
            "intro_animation", "background_music", "interview_recording",
            "banner_ad_final", "thumbnail_v2", "podcast_episode_1",
            "stock_footage", "game_textures", "hero_image_website",
            "voice_memo_001", "wedding_video", "birthday_party",
            "product_photo_front", "product_photo_side", "team_photo_2026",
            "screenshot_dashboard", "screenshot_error", "wallpaper_4k",
            "icon_notification", "icon_settings", "avatar_default",
            "cover_art_album", "mixtape_summer", "ringtone_custom",
            "notification_sound", "ambient_rain", "lofi_beats_study",
            "tutorial_video_part1", "webinar_recording_sept",
            "drone_footage_beach", "timelapse_sunset", "slow_motion_water",
            "concert_live_recording", "audiobook_chapter3",
            "sound_effect_click", "sound_effect_whoosh", "jingle_intro",
            "background_pattern", "texture_wood", "texture_marble",
            "gradient_blue", "mockup_iphone", "mockup_laptop",
            "infographic_sales", "chart_export", "diagram_architecture",
            "scan_document_001", "photo_id_passport", "selfie_gym",
            "pet_photo_max", "food_photography", "landscape_mountains",
            "cityscape_night", "portrait_studio", "macro_flower",
            "aerial_view_campus", "underwater_coral",
        ],
        "extensions": [".jpg", ".png", ".mp4", ".mp3", ".wav", ".svg",
                       ".gif", ".mkv", ".webp", ".flac", ".jpeg",
                       ".bmp", ".tiff", ".avi", ".mov", ".wmv",
                       ".m4a", ".ogg", ".aac", ".webm"],
    },

    "Archives": {
        "stems": [
            "db_backup_2026", "source_code_v1", "old_photos",
            "windows_files", "archive_part1", "mysql_dump_prod",
            "website_backup_complete", "project_final_final",
            "logs_compressed_sept", "email_export_2025",
            "migration_data_bundle", "release_v2.3.1",
            "assets_bundle_production", "fonts_collection",
            "client_deliverables_q3", "server_snapshot_20260901",
            "home_directory_backup", "photos_vacation_2024",
            "music_library_export", "video_raw_footage",
            "design_files_figma", "coursework_semester2",
            "tax_documents_2025", "legal_documents_bundle",
            "system_image_full", "partition_backup_sda1",
            "docker_images_export", "kubernetes_configs",
            "node_modules_cache", "maven_repository_local",
            "gradle_cache_snapshot", "pip_packages_offline",
            "firmware_update_bundle", "driver_pack_win11",
            "game_save_files", "vm_snapshot_ubuntu",
            "time_machine_backup", "rsync_incremental_sept",
            "encrypted_vault", "password_manager_export",
            "certificate_bundle_ssl", "ssh_keys_backup",
        ],
        "extensions": [".zip", ".tar.gz", ".7z", ".rar", ".jar",
                       ".gz", ".bz2", ".tar", ".xz", ".tar.bz2",
                       ".tar.xz", ".tgz", ".war", ".ear"],
    },

    "System & Logs": {
        "stems": [
            "server_error", "access_trace", "application_debug",
            "config_system", "system_settings", "nginx_access",
            "tomcat_catalina", "windows_registry_backup", "bash_history",
            "boot_trace", "kernel_panic", "dmesg_output",
            "syslog_daemon", "auth_failures", "cron_jobs",
            "firewall_rules", "iptables_dump", "network_interfaces",
            "dns_resolver", "dhcp_leases", "apache_error",
            "mysql_slow_query", "postgresql_activity", "redis_server",
            "mongodb_diagnostic", "elasticsearch_gc",
            "docker_daemon", "kubelet_service", "systemd_journal",
            "journalctl_export", "audit_security", "fail2ban_status",
            "logrotate_config", "rsyslog_config", "ntp_drift",
            "swap_usage", "disk_smart_status", "memory_dump",
            "cpu_governor", "thermal_throttle", "battery_health",
            "bluetooth_pairing", "wifi_diagnostics", "vpn_connection",
            "proxy_settings", "hosts_file_backup", "fstab_backup",
            "grub_config", "xorg_display", "pulseaudio_config",
            "pipewire_settings", "environment_variables",
        ],
        "extensions": [".log", ".ini", ".cfg", ".conf", ".out",
                       ".reg", ".sys", ".tmp", ".bak", ".old",
                       ".pid", ".lock", ".env", ".properties"],
    },

    "Executables": {
        "stems": [
            "setup", "install_v2", "update_script",
            "run_server", "app_release", "photoshop_installer",
            "linux_dependencies", "start_mining", "chrome_installer",
            "firefox_setup", "vscode_latest", "node_installer",
            "python3_setup", "java_jdk17", "docker_desktop",
            "git_windows", "steam_setup", "discord_installer",
            "zoom_client", "slack_desktop", "spotify_premium",
            "vlc_media_player", "7zip_installer", "winrar_setup",
            "notepad_plus_plus", "putty_portable", "filezilla_client",
            "wireshark_installer", "virtualbox_setup", "vmware_workstation",
            "nvidia_driver_535", "amd_radeon_driver", "intel_graphics",
            "realtek_audio_driver", "broadcom_wifi", "usb_bootable_creator",
            "partition_manager", "disk_cleanup_tool", "registry_cleaner",
            "malware_scanner", "antivirus_update", "firewall_manager",
            "backup_scheduler", "file_shredder", "process_monitor",
            "system_optimizer", "boot_repair", "grub_installer",
            "pacman_wrapper", "yay_aur_helper", "flatpak_runtime",
            "deploy_production", "rollback_script", "health_check",
        ],
        "extensions": [".exe", ".msi", ".sh", ".bat", ".apk",
                       ".dmg", ".deb", ".rpm", ".cmd", ".bin",
                       ".run", ".AppImage", ".snap", ".pkg",
                       ".com", ".ps1"],
    },
}


def generate_dataset():
    data = []
    seen = set()
    for category, config in CATEGORIES.items():
        stems = config["stems"]
        extensions = config["extensions"]
        # Primary pass: each stem with a primary extension
        for stem in stems:
            ext = random.choice(extensions)
            fn = f"{stem}{ext}"
            if fn not in seen:
                seen.add(fn)
                data.append((fn, category))

        # Secondary pass: realistic variations to achieve a balanced 95 samples per category
        current_cat_count = sum(1 for _, cat in data if cat == category)
        while current_cat_count < 95:
            stem = random.choice(stems)
            ext = random.choice(extensions)
            suffix = random.choice(["_v2", "_old", "_backup", "_final", "_copy", "_2026", "_new", "_temp"])
            fn = f"{stem}{suffix}{ext}"
            if fn not in seen:
                seen.add(fn)
                data.append((fn, category))
                current_cat_count += 1

    random.shuffle(data)
    return data


def get_dataset_stats(data):
    """Return per-category counts."""
    from collections import Counter
    counts = Counter(label for _, label in data)
    return dict(sorted(counts.items()))


if __name__ == "__main__":
    data = generate_dataset()
    stats = get_dataset_stats(data)

    print(f"\n{'='*60}")
    print(f"  DATASET GENERATION COMPLETE")
    print(f"{'='*60}")
    print(f"  Total samples: {len(data)}")
    print(f"\n  Per-category distribution:")
    for cat, count in stats.items():
        print(f"    {cat:25s} → {count:4d} samples")
    print(f"{'='*60}")

    print(f"\n  Sample filenames:")
    for filename, category in data[:10]:
        print(f"    '{filename}' → [{category}]")
    print(f"    ... and {len(data) - 10} more\n")
