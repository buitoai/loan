"""Data models for story processing"""
from pydantic import BaseModel, Field
from typing import List, Optional
from enum import Enum
from datetime import datetime


class JobStatus(str, Enum):
    """Job processing status"""
    PENDING = "pending"
    ANALYZING = "analyzing"
    GENERATING_IMAGES = "generating_images"
    GENERATING_AUDIO = "generating_audio"
    CREATING_VIDEO = "creating_video"
    COMPLETED = "completed"
    FAILED = "failed"


class Scene(BaseModel):
    """A single scene in the story"""
    scene_number: int
    description: str
    narration: str
    visual_prompt: str
    duration: float = 5.0
    image_path: Optional[str] = None
    audio_path: Optional[str] = None


class StoryRequest(BaseModel):
    """Request to convert story to movie"""
    story_text: str = Field(..., min_length=50, description="The story text to convert")
    title: Optional[str] = Field(None, description="Movie title")
    num_scenes: int = Field(10, ge=3, le=30, description="Number of scenes to generate")
    scene_duration: float = Field(5.0, ge=2.0, le=10.0, description="Duration per scene in seconds")
    style: str = Field("anime", description="Visual style: anime, realistic, cartoon, fantasy")
    language: str = Field("en", description="Language code for TTS")
    add_narration: bool = Field(True, description="Add voice narration")


class MovieProject(BaseModel):
    """Movie project with all scenes and metadata"""
    project_id: str
    title: str
    story_text: str
    status: JobStatus
    progress: int = 0
    scenes: List[Scene] = []
    video_path: Optional[str] = None
    error: Optional[str] = None
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: datetime = Field(default_factory=datetime.utcnow)
    
    class Config:
        json_encoders = {
            datetime: lambda v: v.isoformat()
        }


class JobResponse(BaseModel):
    """Response for job submission"""
    project_id: str
    status: JobStatus
    message: str


class ProgressResponse(BaseModel):
    """Progress update response"""
    project_id: str
    status: JobStatus
    progress: int
    current_step: str
    video_path: Optional[str] = None
    error: Optional[str] = None
