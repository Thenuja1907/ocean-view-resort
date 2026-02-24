$body = @{ firstName='Test'; lastName='User'; email='testuser2@example.com'; password='Test@1234'; contactNumber='1234567890'; address='Test Address'; idType='NIC'; idNumber='ID123'; nationality='Nowhere' }
try {
    $resp = Invoke-WebRequest -Uri 'http://localhost:8080/api/guests' -Method Post -Body $body -ContentType 'application/x-www-form-urlencoded' -UseBasicParsing -ErrorAction Stop
    Write-Output "HTTP/200 OK"
    Write-Output $resp.Content
} catch {
    if ($_.Exception.Response -ne $null) {
        $resp = $_.Exception.Response
        Write-Output "HTTP/ERROR - Status: $($resp.StatusCode)"
        try {
            $sr = New-Object System.IO.StreamReader ($resp.GetResponseStream())
            $body = $sr.ReadToEnd()
            Write-Output $body
        } catch { Write-Output "(no response body)" }
    } else {
        Write-Output "ERROR: $($_.Exception.Message)"
    }
}
