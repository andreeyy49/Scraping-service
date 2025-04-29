import { useState } from "react";
import { Container, TextField, Button, Typography, Box } from "@mui/material";
import { scrapSite } from "../api/siteApi";
import { useNavigate } from "react-router-dom";

export default function AddSitePage() {
    const [url, setUrl] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async () => {
        const success = await scrapSite(url);
        if (success) navigate("/"); // Назад на дашборд
    };

    return (
        <Container maxWidth="sm" sx={{ mt: 8 }}>
            <Typography variant="h5" gutterBottom>🕸️ Скрапить новый сайт</Typography>
            <TextField
                fullWidth
                label="URL сайта"
                value={url}
                onChange={(e) => setUrl(e.target.value)}
                margin="normal"
            />
            <Button variant="contained" onClick={handleSubmit}>
                🔍 Скрапить
            </Button>
        </Container>
    );
}
