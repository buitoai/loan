"""Video generation and compilation service"""
import os
from typing import List
from moviepy.editor import (
    ImageClip,
    AudioFileClip,
    CompositeVideoClip,
    concatenate_videoclips
)


class VideoService:
    """Service for creating videos from images and audio"""
    
    def __init__(self):
        self.fps = int(os.getenv("VIDEO_FPS", 24))
    
    async def create_scene_clip(
        self,
        image_path: str,
        audio_path: str = None,
        duration: float = 5.0
    ) -> ImageClip:
        """
        Create a video clip from image and optional audio
        """
        try:
            # Create image clip
            clip = ImageClip(image_path, duration=duration)
            
            # Add audio if provided
            if audio_path and os.path.exists(audio_path):
                audio = AudioFileClip(audio_path)
                # Use audio duration if longer than specified duration
                if audio.duration > duration:
                    duration = audio.duration
                    clip = clip.set_duration(duration)
                clip = clip.set_audio(audio)
            
            return clip
            
        except Exception as e:
            print(f"Error creating scene clip: {e}")
            raise Exception(f"Scene clip creation failed: {str(e)}")
    
    async def compile_movie(
        self,
        scene_clips: List[ImageClip],
        output_path: str
    ) -> str:
        """
        Compile all scene clips into final movie
        """
        try:
            # Concatenate all clips
            final_clip = concatenate_videoclips(
                scene_clips,
                method="compose"
            )
            
            # Write video file
            final_clip.write_videofile(
                output_path,
                fps=self.fps,
                codec='libx264',
                audio_codec='aac',
                preset='medium'
            )
            
            # Close clips
            final_clip.close()
            for clip in scene_clips:
                clip.close()
            
            return output_path
            
        except Exception as e:
            print(f"Error compiling movie: {e}")
            raise Exception(f"Movie compilation failed: {str(e)}")
