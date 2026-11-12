# Story-to-Movie AI 🎬

Hệ thống AI local chuyển đổi truyện thành phim hoạt hình tự động.

## Tính năng chính

- 📖 **Story Analysis**: Phân tích truyện và chia thành scenes
- 🎨 **Image Generation**: Tạo hình ảnh cho từng scene với Stable Diffusion
- 🎥 **Video Generation**: Chuyển ảnh thành video với animation
- 🔊 **Voice Over**: Text-to-speech cho narration
- 🎬 **Movie Compilation**: Ghép các scenes thành phim hoàn chỉnh

## Công nghệ sử dụng

### Backend (Python)
- **FastAPI**: REST API server
- **Ollama**: LLM local (Llama 3.2) cho phân tích truyện
- **Stable Diffusion**: Generate images
- **MoviePy**: Video processing
- **Coqui TTS**: Text-to-speech

### Frontend (React)
- React 18
- Vite
- TailwindCSS
- Axios

## Cài đặt

### 1. Cài đặt Ollama

```bash
# Windows - Download từ https://ollama.ai/download
# Sau khi cài, pull model:
ollama pull llama3.2
```

### 2. Setup Backend

```bash
cd backend
python -m venv venv
venv\Scripts\activate
pip install -r requirements.txt
```

### 3. Setup Frontend

```bash
cd frontend
npm install
```

## Chạy ứng dụng

### Terminal 1 - Backend
```bash
cd backend
venv\Scripts\activate
python main.py
# Server chạy ở http://localhost:8000
```

### Terminal 2 - Frontend
```bash
cd frontend
npm run dev
# App chạy ở http://localhost:5173
```

## Sử dụng

1. Mở trình duyệt tại `http://localhost:5173`
2. Paste hoặc upload file truyện (.txt)
3. Chọn settings (style, số scenes, độ dài...)
4. Click "Generate Movie"
5. Theo dõi progress realtime
6. Download video khi hoàn thành

## Cấu trúc thư mục

```
story-to-movie-ai/
├── backend/
│   ├── app/
│   │   ├── api/          # API endpoints
│   │   ├── services/     # AI services
│   │   ├── models/       # Data models
│   │   └── utils/        # Helper functions
│   ├── output/           # Generated files
│   │   ├── videos/
│   │   ├── images/
│   │   └── audio/
│   └── main.py
├── frontend/
│   └── src/
│       ├── components/   # React components
│       ├── pages/        # Pages
│       └── api/          # API calls
└── README.md
```

## Yêu cầu hệ thống

- **RAM**: 16GB+ khuyến nghị
- **GPU**: NVIDIA GPU với CUDA (khuyến nghị cho Stable Diffusion)
- **Disk**: 20GB+ free space
- **OS**: Windows 10/11, Linux, macOS

## Lưu ý

- Lần đầu chạy sẽ tải models (~5-10GB)
- Tạo 1 phút video mất khoảng 5-15 phút tùy GPU
- Có thể chạy không GPU nhưng sẽ chậm hơn nhiều

## Roadmap

- [ ] Support thêm models (SDXL, AnimateDiff)
- [ ] Music generation
- [ ] Multiple voice characters
- [ ] Real-time preview
- [ ] Export nhiều formats

## License

MIT License
