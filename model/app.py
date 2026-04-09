from fastapi import FastAPI
from pydantic import BaseModel
import uvicorn
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.pipeline import make_pipeline

app = FastAPI()

training_data = [
    # --- Development & Code ---
    ("MainApp.java", "Development & Code"),
    ("utils.py", "Development & Code"),
    ("index.html", "Development & Code"),
    ("style.css", "Development & Code"),
    ("package.json", "Development & Code"),
    ("script.js", "Development & Code"),
    ("database_schema.sql", "Development & Code"),
    ("Dockerfile", "Development & Code"),
    (".gitignore", "Development & Code"),
    ("pom.xml", "Development & Code"),
    ("application.properties", "Development & Code"),
    ("auth_controller.cpp", "Development & Code"),
    ("header.h", "Development & Code"),
    ("build.gradle", "Development & Code"),
    ("tsconfig.json", "Development & Code"),
    ("app_module.ts", "Development & Code"),
    ("FileNetworkService.java", "Development & Code"),
    ("docker-compose.yml", "Development & Code"),

    # --- Documents & Finance ---
    ("Q1_Financial_Report.pdf", "Documents"),
    ("employee_handbook.docx", "Documents"),
    ("Q3_Budget_Forecast.xlsx", "Documents"),
    ("marketing_strategy.pptx", "Documents"),
    ("client_meeting_notes.txt", "Documents"),
    ("invoice_10425.pdf", "Documents"),
    ("tax_return_2025.csv", "Documents"),
    ("vendor_contract_v2.docx", "Documents"),
    ("company_policies.pdf", "Documents"),
    ("sales_pitch_deck.ppt", "Documents"),
    ("expense_report_january.xls", "Documents"),
    ("NDA_signed.pdf", "Documents"),
    ("resume_harsh.docx", "Documents"),
    ("cover_letter.pdf", "Documents"),

    # --- Media & Assets ---
    ("profile_picture.jpg", "Media & Assets"),
    ("vacation_hawaii.png", "Media & Assets"),
    ("logo_transparent.svg", "Media & Assets"),
    ("intro_animation.mp4", "Media & Assets"),
    ("background_music.mp3", "Media & Assets"),
    ("interview_recording.wav", "Media & Assets"),
    ("banner_ad_final.gif", "Media & Assets"),
    ("thumbnail_v2.jpeg", "Media & Assets"),
    ("podcast_episode_1.m4a", "Media & Assets"),
    ("stock_footage.mov", "Media & Assets"),
    ("game_textures.dds", "Media & Assets"),
    ("hero_image_website.webp", "Media & Assets"),
    ("voice_memo_001.ogg", "Media & Assets"),

    # --- Archives & Backups ---
    ("db_backup_2026.zip", "Archives & Backups"),
    ("source_code_v1.tar.gz", "Archives & Backups"),
    ("old_photos.rar", "Archives & Backups"),
    ("windows_files.7z", "Archives & Backups"),
    ("archive_part1.zip", "Archives & Backups"),
    ("mysql_dump_prod.sql.gz", "Archives & Backups"),
    ("website_backup_complete.tar", "Archives & Backups"),
    ("project_final_final.zip", "Archives & Backups"),

    # --- System & Logs ---
    ("server_error.log", "System Logs"),
    ("access_trace.log", "System Logs"),
    ("application_debug.log", "System Logs"),
    ("config.ini", "System Logs"),
    ("system_settings.cfg", "System Logs"),
    ("nginx_access.log", "System Logs"),
    ("tomcat_catalina.out", "System Logs"),
    ("windows_registry_backup.reg", "System Logs"),
    ("bash_history.txt", "System Logs"),
    ("boot_trace.log", "System Logs"),

    # --- Executables & Installers ---
    ("setup.exe", "Executables & Installers"),
    ("install_v2.msi", "Executables & Installers"),
    ("update_script.sh", "Executables & Installers"),
    ("run_server.bat", "Executables & Installers"),
    ("app_release.apk", "Executables & Installers"),
    ("photoshop_installer.dmg", "Executables & Installers"),
    ("linux_dependencies.deb", "Executables & Installers"),
    ("start_mining.cmd", "Executables & Installers")
]

# Separate features (filenames) and labels (categories)
X_train = [item[0] for item in training_data]
y_train = [item[1] for item in training_data]

# =====================================================================
# 2. MODEL PIPELINE CREATION
# TfidfVectorizer with analyzer='char_wb' breaks strings into character 
# chunks (n-grams). This is crucial! It means the AI learns that ".pdf" 
# belongs to Business, and ".java" belongs to Code, without us having 
# to write a single IF statement.
# =====================================================================
print("Initializing and training the AI Model...")
model = make_pipeline(
    TfidfVectorizer(analyzer='char_wb', ngram_range=(2, 5)), 
    MultinomialNB()
)
model.fit(X_train, y_train)
print("AI Model successfully trained and loaded into memory!")

# =====================================================================
# 3. FASTAPI ENDPOINTS
# =====================================================================
class FileData(BaseModel):
    filename: str

@app.post("/predict")
def predict_category(data: FileData):
    # The AI looks at the string it receives and calculates the highest probability category
    prediction = model.predict([data.filename])[0]
    
    # Print to the Python console so you can show the professor it working in real-time
    print(f"AI Analyzed: '{data.filename}' --> Categorized as: [{prediction}]")
    
    return {"category": prediction}

@app.get("/health")
def health_check():
    return {"status": "AI Microservice is running and ready."}

if __name__ == "__main__":
    print("Starting Mini Cloud AI Categorization Engine on Port 8000...")
    uvicorn.run(app, host="127.0.0.1", port=8000)