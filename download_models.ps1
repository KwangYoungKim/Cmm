$baseUrl = "https://raw.githubusercontent.com/justadudewhohacks/face-api.js/master/weights/"
$models = @(
    "ssd_mobilenetv1_model-weights_manifest.json",
    "ssd_mobilenetv1_model-shard1",
    "ssd_mobilenetv1_model-shard2",
    "face_landmark_68_model-weights_manifest.json",
    "face_landmark_68_model-shard1",
    "face_recognition_model-weights_manifest.json",
    "face_recognition_model-shard1",
    "face_recognition_model-shard2",
    "tiny_face_detector_model-weights_manifest.json",
    "tiny_face_detector_model-shard1"
)

Write-Host "Downloading face-api.min.js..."
curl.exe -L -o "src/main/resources/static/js/face-api.min.js" "https://raw.githubusercontent.com/justadudewhohacks/face-api.js/master/dist/face-api.min.js"

foreach ($m in $models) {
    Write-Host "Downloading model: $m"
    curl.exe -L -o ("src/main/resources/static/models/" + $m) ($baseUrl + $m)
}
Write-Host "Download complete!"
