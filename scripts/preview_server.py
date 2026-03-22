import http.server
import os

class NoCacheHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "public"), **kwargs)

    def end_headers(self):
        self.send_header("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
        self.send_header("Pragma", "no-cache")
        self.send_header("Expires", "0")
        super().end_headers()

if __name__ == "__main__":
    server = http.server.HTTPServer(("0.0.0.0", 5000), NoCacheHandler)
    print("Preview server running on port 5000 (no-cache)")
    server.serve_forever()
