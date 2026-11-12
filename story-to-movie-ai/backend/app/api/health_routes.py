"""Health check endpoints"""
from fastapi import APIRouter
from pydantic import BaseModel
import os

router = APIRouter()


class HealthResponse(BaseModel):
    status: str
    ollama_available: bool
    gpu_available: bool
    output_dir: str


@router.get("/health", response_model=HealthResponse)
async def health_check():
    """Check system health and availability"""
    import torch
    import requests
    
    # Check Ollama
    ollama_available = False
    try:
        ollama_host = os.getenv("OLLAMA_HOST", "http://localhost:11434")
        response = requests.get(f"{ollama_host}/api/tags", timeout=2)
        ollama_available = response.status_code == 200
    except:
        pass
    
    # Check GPU
    gpu_available = torch.cuda.is_available()
    
    return HealthResponse(
        status="healthy",
        ollama_available=ollama_available,
        gpu_available=gpu_available,
        output_dir=os.getenv("OUTPUT_DIR", "./output")
    )
