$DATA_DIR="./minio_data"
if ( !(test-path -PathType container $DATA_DIR) )
{
    New-Item -ItemType Directory -Path $DATA_DIR
    Write-Host "Создана директория для данных: $DATA_DIR"
}

Write-Host "Запуск MinIO на порту 9000 и консоль на порту 9001..."
.\minio.exe server "$DATA_DIR" --console-address ":9001"

$process = Get-Process | Where-Object { $_.ProcessName -like "*minio*" }

if ($process)
{
    Write-Host "MinIO успешно запущен!"
    Write-Host "Консоль доступна по адресу:"
} else {
    Write-Host "Ошибка при запуске MinIO"
}