"""Image generation service using Stable Diffusion"""
import os
import torch
from diffusers import StableDiffusionPipeline
from PIL import Image


class ImageService:
    """Service for generating images with Stable Diffusion"""
    
    def __init__(self):
        self.model_id = os.getenv("SD_MODEL", "stabilityai/stable-diffusion-2-1")
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        self.pipe = None
        
    def load_model(self):
        """Load Stable Diffusion model"""
        if self.pipe is None:
            print(f"Loading Stable Diffusion model: {self.model_id}")
            
            self.pipe = StableDiffusionPipeline.from_pretrained(
                self.model_id,
                torch_dtype=torch.float16 if self.device == "cuda" else torch.float32,
                safety_checker=None  # Disable for speed
            )
            
            self.pipe = self.pipe.to(self.device)
            
            # Enable memory optimizations
            if self.device == "cuda":
                self.pipe.enable_attention_slicing()
                # self.pipe.enable_xformers_memory_efficient_attention()
            
            print(f"Model loaded on {self.device}")
    
    async def generate_image(
        self,
        prompt: str,
        output_path: str,
        width: int = 512,
        height: int = 512,
        num_inference_steps: int = 30
    ) -> str:
        """
        Generate image from text prompt
        """
        self.load_model()
        
        try:
            # Generate image
            with torch.inference_mode():
                result = self.pipe(
                    prompt=prompt,
                    width=width,
                    height=height,
                    num_inference_steps=num_inference_steps,
                    guidance_scale=7.5
                )
            
            # Save image
            image = result.images[0]
            image.save(output_path)
            
            return output_path
            
        except Exception as e:
            print(f"Error generating image: {e}")
            raise Exception(f"Image generation failed: {str(e)}")
    
    def cleanup(self):
        """Free up memory"""
        if self.pipe is not None:
            del self.pipe
            self.pipe = None
            
            if torch.cuda.is_available():
                torch.cuda.empty_cache()
