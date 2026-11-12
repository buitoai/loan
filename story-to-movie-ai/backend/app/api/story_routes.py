"""Story processing API endpoints"""
from fastapi import APIRouter, HTTPException, BackgroundTasks
from typing import Dict
import uuid

from app.models import StoryRequest, JobResponse, ProgressResponse, JobStatus
from app.services.movie_generator import MovieGenerator

router = APIRouter()

# In-memory job storage (use Redis/DB in production)
jobs: Dict[str, dict] = {}

# Movie generator instance
generator = MovieGenerator()


@router.post("/story/generate", response_model=JobResponse)
async def generate_movie(
    request: StoryRequest,
    background_tasks: BackgroundTasks
):
    """
    Submit a story for movie generation
    """
    # Create project ID
    project_id = str(uuid.uuid4())
    
    # Initialize job
    jobs[project_id] = {
        "status": JobStatus.PENDING,
        "progress": 0,
        "current_step": "Initializing...",
        "video_path": None,
        "error": None
    }
    
    # Start processing in background
    background_tasks.add_task(
        process_story,
        project_id=project_id,
        request=request
    )
    
    return JobResponse(
        project_id=project_id,
        status=JobStatus.PENDING,
        message="Movie generation started. Use /api/story/progress/{project_id} to track progress."
    )


@router.get("/story/progress/{project_id}", response_model=ProgressResponse)
async def get_progress(project_id: str):
    """
    Get progress of a movie generation job
    """
    if project_id not in jobs:
        raise HTTPException(status_code=404, detail="Project not found")
    
    job = jobs[project_id]
    
    return ProgressResponse(
        project_id=project_id,
        status=job["status"],
        progress=job["progress"],
        current_step=job["current_step"],
        video_path=job["video_path"],
        error=job["error"]
    )


@router.get("/story/list")
async def list_projects():
    """
    List all projects
    """
    return {
        "projects": [
            {
                "project_id": pid,
                "status": job["status"],
                "progress": job["progress"]
            }
            for pid, job in jobs.items()
        ]
    }


@router.delete("/story/{project_id}")
async def delete_project(project_id: str):
    """
    Delete a project and its files
    """
    if project_id not in jobs:
        raise HTTPException(status_code=404, detail="Project not found")
    
    # TODO: Delete files from disk
    del jobs[project_id]
    
    return {"message": "Project deleted successfully"}


async def process_story(project_id: str, request: StoryRequest):
    """
    Background task to process story and generate movie
    """
    try:
        # Update status callback
        def update_progress(status: JobStatus, progress: int, step: str):
            jobs[project_id]["status"] = status
            jobs[project_id]["progress"] = progress
            jobs[project_id]["current_step"] = step
        
        # Generate movie
        video_path = await generator.generate_movie(
            request=request,
            progress_callback=update_progress
        )
        
        # Update final status
        jobs[project_id]["status"] = JobStatus.COMPLETED
        jobs[project_id]["progress"] = 100
        jobs[project_id]["current_step"] = "Completed!"
        jobs[project_id]["video_path"] = video_path
        
    except Exception as e:
        jobs[project_id]["status"] = JobStatus.FAILED
        jobs[project_id]["error"] = str(e)
        jobs[project_id]["current_step"] = "Failed"
        print(f"Error processing story: {e}")
