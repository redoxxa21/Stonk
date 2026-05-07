curl.exe -X POST http://127.0.0.1:8081/auth/register -H "Content-Type: application/json" -d @test_register.json > output.txt 2>&1
type output.txt
