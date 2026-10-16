import matplotlib.pyplot as plt
import matplotlib.patches as patches

# Setup the figure with high resolution
fig, ax = plt.subplots(figsize=(20, 11.5), dpi=300)
ax.set_xlim(0, 20)
ax.set_ylim(0, 11.5)
ax.axis('off')

# Professional Color Palette
c_navy      = '#1e3a8a'  # Client
c_emerald   = '#065f46'  # Security
c_forest    = '#14532d'  # Backend / App
c_amber     = '#9a3412'  # Database
c_storage   = '#b45309'  # Storage
c_purple    = '#581c87'  # AI Service
c_border    = '#cbd5e1'
c_bg_lane   = '#f8fafc'
c_card_bg   = '#ffffff'
c_title     = '#0f172a'
c_subtitle  = '#475569'

# Draw Swimlane / Tier Boundary
def draw_tier(x, y, w, h, title, subtitle):
    # Background card
    lane = patches.FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.0,rounding_size=0.25",
                                 linewidth=1.5, edgecolor=c_border, facecolor=c_bg_lane, alpha=0.9)
    ax.add_patch(lane)
    # Header badge
    ax.text(x + w/2, y + h - 0.35, title, ha='center', va='center',
            color=c_title, fontsize=12, fontweight='bold')
    if subtitle:
        ax.text(x + w/2, y + h - 0.65, subtitle, ha='center', va='center',
                color=c_subtitle, fontsize=9, fontstyle='italic')

# Draw Component Card with dedicated colored header strip and bullet points
def draw_card(x, y, w, h, title, tag, items, header_color):
    # Card shadow / border
    card = patches.FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.0,rounding_size=0.18",
                                  linewidth=1.2, edgecolor='#94a3b8', facecolor=c_card_bg)
    ax.add_patch(card)
    
    # Colored header banner
    header_h = 0.8
    header = patches.FancyBboxPatch((x, y + h - header_h), w, header_h, 
                                    boxstyle="round,pad=0.0,rounding_size=0.18",
                                    linewidth=0, facecolor=header_color)
    ax.add_patch(header)
    
    # Square off the bottom of the header strip
    filler = patches.Rectangle((x, y + h - header_h), w, 0.25, linewidth=0, facecolor=header_color)
    ax.add_patch(filler)
    
    # Header text
    ax.text(x + 0.25, y + h - 0.35, title, ha='left', va='center',
            color='#ffffff', fontsize=11, fontweight='bold')
    if tag:
        ax.text(x + w - 0.25, y + h - 0.35, tag, ha='right', va='center',
                color='#e2e8f0', fontsize=8.5, fontweight='semibold')
        
    # Divider line under header
    ax.plot([x, x + w], [y + h - header_h, y + h - header_h], color='#e2e8f0', lw=1)
    
    # Items / bullets
    start_y = y + h - header_h - 0.32
    for i, item in enumerate(items):
        cur_y = start_y - (i * 0.30)
        # Bullet dot
        ax.plot(x + 0.3, cur_y, marker='o', markersize=3.5, color=header_color)
        # Text
        ax.text(x + 0.5, cur_y, item, ha='left', va='center',
                color='#1e293b', fontsize=8.8, fontweight='normal')

# Draw clean annotated connection arrows
def draw_connector(x1, y1, x2, y2, label=None, label_pos=0.5, offset_y=0.2, rad=0.0):
    connectionstyle = f"arc3,rad={rad}" if rad != 0 else "arc3"
    ax.annotate("",
                xy=(x2, y2), xycoords='data',
                xytext=(x1, y1), textcoords='data',
                arrowprops=dict(arrowstyle="-|>", color='#475569', lw=2.0, 
                                mutation_scale=14, connectionstyle=connectionstyle))
    if label:
        lx = x1 + (x2 - x1) * label_pos
        ly = y1 + (y2 - y1) * label_pos + offset_y
        ax.text(lx, ly, label, ha='center', va='center', color='#0f172a',
                fontsize=8.2, fontweight='bold',
                bbox=dict(boxstyle="round,pad=0.25,rounding_size=0.1", fc="#ffffff", ec="#94a3b8", lw=0.8, alpha=0.95))

# ==================== MAIN HEADER ====================
ax.text(10, 11.0, "INTELLIGENT CLOUD DRIVE — SYSTEM ARCHITECTURE", 
        ha='center', va='center', fontsize=17, fontweight='bold', color='#0f172a')
ax.text(10, 10.65, "End-to-End Multi-Tier Distributed Cloud File Management & NLP Intelligence Pipeline", 
        ha='center', va='center', fontsize=10.5, color='#475569')

# Orchestration Enclosure (Docker / Network boundary)
docker_env = patches.FancyBboxPatch((0.4, 0.4), 19.2, 9.9, boxstyle="round,pad=0.0,rounding_size=0.3",
                                    linewidth=1.8, edgecolor='#0284c7', facecolor='#f0f9ff', alpha=0.4, linestyle=':')
ax.add_patch(docker_env)
ax.text(0.7, 10.1, "Distributed Microservices Environment (Spring Boot + FastAPI + MariaDB + Docker)", 
        ha='left', va='center', fontsize=9.5, fontweight='bold', color='#0369a1')

# ==================== TIERS ====================

# 1. Client Tier
draw_tier(0.6, 0.6, 3.8, 9.3, "TIER 1: PRESENTATION LAYER", "Native Desktop Application")
draw_card(0.8, 1.0, 3.4, 8.3, "JavaFX Desktop Client", "Java 13 / Maven", [
    "SceneManager Dynamic Screen Navigation",
    "Login & Registration Views",
    "Dashboard with Interactive File Grid",
    "Real-time AI Category & Size Display",
    "Multipart File Upload & Streaming",
    "Secure Download & File Saving",
    "Category Filtering & Instant Search",
    "In-Memory JWT Secure Token Vault",
    "ServerConfig Dynamic IP Resolution",
    "Local File System Bridge Access"
], c_navy)

# 2. Security & Gateway Tier
draw_tier(4.7, 0.6, 3.8, 9.3, "TIER 2: SECURITY & GATEWAY", "Stateless Authentication & Defense")
draw_card(4.9, 5.4, 3.4, 3.9, "Stateless JWT Auth Filter", "OncePerRequest", [
    "Intercepts Every Inbound HTTP Request",
    "Extracts 'Authorization: Bearer <token>'",
    "HMAC-SHA256 Cryptographic Verification",
    "Claims & Subject (Username) Parsing",
    "Populates Spring SecurityContextHolder",
    "Enforces 24-Hour Expiration Lifespan",
    "Strict Stateless Session Policy"
], c_emerald)

draw_card(4.9, 1.0, 3.4, 4.0, "Input Defense & Sanitization", "Zero-Trust", [
    "Path Traversal Prevention (OWASP)",
    "StringUtils.cleanPath() Normalization",
    "Checks for Directory Breakout (..)",
    "IDOR Authorization Verification",
    "File Size Enforcement (Max 15MB)",
    "FileUploadExceptionHandler Catching",
    "Strict MIME-Type & Extension Checks"
], c_emerald)

# 3. Application Tier
draw_tier(8.8, 0.6, 4.6, 9.3, "TIER 3: APPLICATION SERVER", "Spring Boot 4.0.3 REST Backend (:8080)")
draw_card(9.0, 7.0, 4.2, 2.3, "AuthController", "REST API", [
    "POST /api/auth/register (User Creation)",
    "POST /api/auth/login (Credential Auth)",
    "BCrypt Password Hashing (Cost 10)",
    "Returns Signed JWT Token Payload"
], c_forest)

draw_card(9.0, 3.9, 4.2, 2.8, "FileController", "REST API", [
    "POST /api/files/upload (Multipart/form)",
    "GET /api/files/download/{id} (Byte Stream)",
    "GET /api/files/list (User File Metadata)",
    "DELETE /api/files/delete/{id}",
    "UUID-Prepending Collision Defense",
    "Enforces User-File Isolation (IDOR)"
], c_forest)

draw_card(9.0, 1.0, 4.2, 2.6, "AiCategorizationService", "Service Layer", [
    "Synchronous REST Client (RestTemplate)",
    "Queries FastAPI /predict Endpoint",
    "Transmits Extracted File Name",
    "Parses AI Predicted Category",
    "Graceful Fallback to 'Uncategorized'"
], c_forest)

# 4. Data & Intelligence Tier
draw_tier(13.7, 0.6, 5.7, 9.3, "TIER 4: DATA & INTELLIGENCE", "Relational Persistence & ML Engine")

# MariaDB Card
draw_card(13.9, 6.8, 5.3, 2.5, "MariaDB / MySQL Database", "Port 3306", [
    "Database: cloud_db | JPA / Hibernate ORM",
    "Users Table: id, username, password (BCrypt)",
    "FileMetadata Table: id, original_name, stored_name,",
    "   file_size, category, upload_date, user_id (FK)",
    "ACID Transaction Guarantee & Referential Integrity"
], c_amber)

# Physical Storage Card
draw_card(13.9, 4.7, 5.3, 1.8, "Physical Cloud Storage", "Local / Cloud Volume", [
    "Directory: ./cloud_storage/ (Isolated Volume)",
    "UUID-based Storage: {uuid}_{originalName}",
    "Zero Filename Collision Guarantee"
], c_storage)

# AI Microservice Card
draw_card(13.9, 1.0, 5.3, 3.4, "FastAPI AI Microservice", "Port 8000 | Python", [
    "High-Performance Asynchronous ASGI (Uvicorn)",
    "POST /predict & GET /health Endpoints",
    "TF-IDF Vectorizer with Character N-Grams (n=2..5)",
    "Linear SVM Classifier (CalibratedClassifierCV)",
    "Trained on 570 Balanced Filename Samples",
    "Achieved 96.49% Test Accuracy & 0.965 F1",
    "Joblib Model Deserialization in RAM"
], c_purple)

# ==================== CONNECTORS / FLOW ====================

# 1. Client to Security Gateway
draw_connector(4.2, 7.5, 4.9, 7.5, "1. POST /login (Credentials)", label_pos=0.5, offset_y=0.22)
draw_connector(4.2, 3.0, 4.9, 3.0, "2. Multipart Upload / Req\n(Bearer JWT Token)", label_pos=0.5, offset_y=0.28)

# 2. Security Gateway to Application Controllers
draw_connector(8.3, 7.5, 9.0, 7.5, "Validated Auth", label_pos=0.5, offset_y=0.22)
draw_connector(8.3, 4.8, 9.0, 4.8, "Sanitized File Stream", label_pos=0.5, offset_y=0.22)

# 3. AuthController to MariaDB
draw_connector(13.2, 8.0, 13.9, 8.0, "Verify / Save User (JPA)", label_pos=0.5, offset_y=0.22)

# 4. FileController to MariaDB
draw_connector(13.2, 6.0, 13.9, 7.2, "Persist FileMetadata (FK)", label_pos=0.45, offset_y=0.22, rad=-0.08)

# 5. FileController to Physical Storage
draw_connector(13.2, 5.3, 13.9, 5.3, "Write Binary File Stream", label_pos=0.5, offset_y=0.22)

# 6. FileController to AiCategorizationService
draw_connector(11.1, 3.9, 11.1, 3.6, "Request Categorization", label_pos=0.5, offset_y=0.0)

# 7. AiCategorizationService to FastAPI Microservice
draw_connector(13.2, 2.3, 13.9, 2.3, "HTTP GET /predict?filename=...\nReturns JSON {category}", label_pos=0.5, offset_y=0.28)

plt.tight_layout()
output_path = '/home/fallen/git/mini_Drive/IDP/proper_system_architecture.png'
plt.savefig(output_path, dpi=300, bbox_inches='tight')
print(f"Generated clean, non-overlapping architecture diagram at: {output_path}")
