"""Main movie generation orchestrator"""
import os
from typing import Callable
from datetime import datetime

from app.models import StoryRequest, JobStatus, Scene
from app.services.ollama_service import OllamaService
from app.services.image_service import ImageService
from app.services.tts_service import TTSService
from app.services.video_service import VideoService


class MovieGenerator:
    """Main orchestrator for movie generation pipeline"""
    
    def __init__(self):
        self.ollama = OllamaService()
        self.image_service = ImageService()
        self.tts_service = TTSService()
        self.video_service = VideoService()
        
        self.output_dir = os.getenv("OUTPUT_DIR", "./output")
    
    async def generate_movie(
        self,
        request: StoryRequest,
        progress_callback: Callable = None
    ) -> str:
        """
        Generate complete movie from story
        
        Pipeline:
        1. Analyze story with Ollama → scenes
        2. Generate images for each scene
        3. Generate narration audio
        4. Compile video
        """
        try:
            # Create project directory
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            project_name = request.title or f"movie_{timestamp}"
            project_dir = os.path.join(self.output_dir, project_name)
            os.makedirs(project_dir, exist_ok=True)
            
            images_dir = os.path.join(project_dir, "images")
            audio_dir = os.path.join(project_dir, "audio")
            os.makedirs(images_dir, exist_ok=True)
            os.makedirs(audio_dir, exist_ok=True)
            
            # Step 1: Analyze story
            if progress_callback:
                progress_callback(JobStatus.ANALYZING, 10, "Analyzing story with AI...")
            
            scenes = await self.ollama.analyze_story(
                story_text=request.story_text,
                num_scenes=request.num_scenes,
                style=request.style
            )
            
            print(f"Generated {len(scenes)} scenes")
            
            # Step 2: Generate images
            if progress_callback:
                progress_callback(JobStatus.GENERATING_IMAGES, 20, "Generating images...")
            
            for i, scene in enumerate(scenes):
                # Enhance prompt
                enhanced_prompt = self.ollama.enhance_visual_prompt(
                    scene.visual_prompt,
                    request.style
                )
                
                # Generate image
                image_path = os.path.join(images_dir, f"scene_{i+1:03d}.png")
                await self.image_service.generate_image(
                    prompt=enhanced_prompt,
                    output_path=image_path
                )
                scene.image_path = image_path
                
                # Update progress
                progress = 20 + int(50 * (i + 1) / len(scenes))
                if progress_callback:
                    progress_callback(
                        JobStatus.GENERATING_IMAGES,
                        progress,
                        f"Generated image {i+1}/{len(scenes)}"
                    )
                
                print(f"Generated image {i+1}/{len(scenes)}")
            
            # Step 3: Generate audio narration
            if request.add_narration:
                if progress_callback:
                    progress_callback(JobStatus.GENERATING_AUDIO, 70, "Generating narration...")
                
                for i, scene in enumerate(scenes):
                    audio_path = os.path.join(audio_dir, f"narration_{i+1:03d}.wav")
                    await self.tts_service.generate_speech(
                        text=scene.narration,
                        output_path=audio_path
                    )
                    scene.audio_path = audio_path
                    
                    progress = 70 + int(15 * (i + 1) / len(scenes))
                    if progress_callback:
                        progress_callback(
                            JobStatus.GENERATING_AUDIO,
                            progress,
                            f"Generated audio {i+1}/{len(scenes)}"
                        )
                    
                    print(f"Generated audio {i+1}/{len(scenes)}")
            
            # Step 4: Create video
            if progress_callback:
                progress_callback(JobStatus.CREATING_VIDEO, 85, "Compiling final movie...")
            
            # Create scene clips
            scene_clips = []
            for scene in scenes:
                clip = await self.video_service.create_scene_clip(
                    image_path=scene.image_path,
                    audio_path=scene.audio_path if request.add_narration else None,
                    duration=request.scene_duration
                )
                scene_clips.append(clip)
            
            # Compile final movie
            video_path = os.path.join(project_dir, f"{project_name}.mp4")
            await self.video_service.compile_movie(
                scene_clips=scene_clips,
                output_path=video_path
            )
            
            print(f"Movie created: {video_path}")
            
            # Cleanup
            self.image_service.cleanup()
            
            # Return relative path for serving
            relative_path = os.path.relpath(video_path, self.output_dir)
            return f"/output/{relative_path}"
            
        except Exception as e:
            print(f"Error in movie generation pipeline: {e}")
            raise
