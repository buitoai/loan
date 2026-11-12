"""Ollama service for story analysis"""
import os
import json
from typing import List
import ollama

from app.models import Scene


class OllamaService:
    """Service for interacting with Ollama LLM"""
    
    def __init__(self):
        self.host = os.getenv("OLLAMA_HOST", "http://localhost:11434")
        self.model = os.getenv("OLLAMA_MODEL", "llama3.2")
        
    async def analyze_story(
        self,
        story_text: str,
        num_scenes: int,
        style: str
    ) -> List[Scene]:
        """
        Analyze story and break it into scenes with visual descriptions
        """
        prompt = f"""You are a movie director. Analyze this story and break it into exactly {num_scenes} cinematic scenes.

Story:
{story_text}

For each scene, provide:
1. A brief description of what happens
2. The narration text (what the narrator would say)
3. A detailed visual prompt for image generation in {style} style

Respond ONLY with a valid JSON array, no other text:
[
  {{
    "scene_number": 1,
    "description": "brief scene description",
    "narration": "what narrator says",
    "visual_prompt": "detailed visual description for {style} style image generation"
  }}
]

Make visual prompts detailed and cinematic. Include: composition, lighting, mood, colors, character details."""

        try:
            # Call Ollama
            response = ollama.chat(
                model=self.model,
                messages=[{
                    'role': 'user',
                    'content': prompt
                }]
            )
            
            # Extract JSON from response
            content = response['message']['content']
            
            # Try to find JSON in response
            start_idx = content.find('[')
            end_idx = content.rfind(']') + 1
            
            if start_idx >= 0 and end_idx > start_idx:
                json_str = content[start_idx:end_idx]
                scenes_data = json.loads(json_str)
                
                # Convert to Scene objects
                scenes = []
                for scene_data in scenes_data[:num_scenes]:
                    scenes.append(Scene(
                        scene_number=scene_data['scene_number'],
                        description=scene_data['description'],
                        narration=scene_data['narration'],
                        visual_prompt=scene_data['visual_prompt']
                    ))
                
                return scenes
            else:
                raise ValueError("No valid JSON found in response")
                
        except Exception as e:
            print(f"Error analyzing story: {e}")
            raise Exception(f"Failed to analyze story: {str(e)}")
    
    def enhance_visual_prompt(self, prompt: str, style: str) -> str:
        """
        Enhance visual prompt with style-specific details
        """
        style_prefixes = {
            "anime": "anime style, studio ghibli inspired, vibrant colors, detailed",
            "realistic": "photorealistic, 8k, highly detailed, professional photography",
            "cartoon": "cartoon style, pixar inspired, colorful, family friendly",
            "fantasy": "fantasy art, magical, ethereal, detailed illustration"
        }
        
        prefix = style_prefixes.get(style, "")
        return f"{prefix}, {prompt}, masterpiece, best quality"
