"""
Story-to-Movie AI - Main FastAPI Application
"""
import os
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from dotenv import load_dotenv

from app.api import story_routes, health_routes

# Load environment variables
load_dotenv()

# Initialize FastAPI app
app = FastAPI(
    title="Story-to-Movie AI",
    description="Convert stories into animated movies using local AI models",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount output directory for serving generated files
output_dir = os.getenv("OUTPUT_DIR", "./output")
os.makedirs(output_dir, exist_ok=True)
app.mount("/output", StaticFiles(directory=output_dir), name="output")

# Include routers
app.include_router(health_routes.router, prefix="/api", tags=["Health"])
app.include_router(story_routes.router, prefix="/api", tags=["Story"])

@app.on_event("startup")
async def startup_event():
    """Initialize services on startup"""
    print("🚀 Story-to-Movie AI is starting up...")
    print(f"📁 Output directory: {output_dir}")
    print(f"🤖 Ollama host: {os.getenv('OLLAMA_HOST', 'http://localhost:11434')}")
    print(f"🎨 SD Model: {os.getenv('SD_MODEL', 'stabilityai/stable-diffusion-2-1')}")
    print(f"🔊 TTS Model: {os.getenv('TTS_MODEL', 'tts_models/en/ljspeech/tacotron2-DDC')}")

@app.on_event("shutdown")
async def shutdown_event():
    """Cleanup on shutdown"""
    print("👋 Story-to-Movie AI is shutting down...")

if __name__ == "__main__":
    import uvicorn
    
    host = os.getenv("HOST", "0.0.0.0")
    port = int(os.getenv("PORT", 8000))
    debug = os.getenv("DEBUG", "True").lower() == "true"
    
    print(f"\n🎬 Starting server at http://{host}:{port}")
    print(f"📚 API docs at http://{host}:{port}/docs\n")
    
    uvicorn.run(
        "main:app",
        host=host,
        port=port,
        reload=debug,
        log_level="info"
    )
