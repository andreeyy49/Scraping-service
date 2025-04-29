import {useState, useEffect} from "react";
import {
    Box,
    CircularProgress,
    Container,
    Typography,
    Button,
    Stack,
    Drawer,
    TextField,
    IconButton,
    AppBar,
    Toolbar, Divider
} from "@mui/material";
import SiteCard from "../components/SiteCard";
import {useNavigate} from "react-router-dom";
import axios from "axios";
import MenuIcon from '@mui/icons-material/Menu';
import StoreIcon from '@mui/icons-material/Store'; // Иконка магазина
import ArticleIcon from '@mui/icons-material/Article'; // Иконка блога/новостей

// Типизация для списка сайтов
interface Site {
    id: string;
    url: string;
    name: string;
    lastScraped: string;
    type: string;
    pageCount: number;
    status: "INDEXING" | "INDEXED" | "FAILED";
}

export default function DashboardPage() {
    const [sites, setSites] = useState<Site[]>([]);
    const [loading, setLoading] = useState(true);
    const [user, setUser] = useState<User | null>(null);
    const [openDrawer, setOpenDrawer] = useState(false);
    const [url, setUrl] = useState("");
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await fetch("/api/v1/auth/logout", {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${localStorage.getItem("token")}`,
                },
            });
        } catch (error) {
            console.error("Ошибка при выходе из системы", error);
        } finally {
            localStorage.removeItem("token");
            navigate("/login");
        }
    };

    useEffect(() => {
        const token = localStorage.getItem("token");

        if (isTokenInvalid(token)) {
            navigate("/login");
            return;
        }

        const fetchUser = async () => {
            try {
                const response = await axios.get("http://localhost:8080/api/v1/user/findThisAccount");
                setUser(response.data);
            } catch (error: any) {
                if (error.response?.status === 401) {
                    localStorage.removeItem("token");
                    navigate("/login");
                } else {
                    console.error("Ошибка при получении пользователя", error);
                }
            } finally {
                setLoading(false);
            }
        };

        fetchUser();
    }, [navigate]);

    useEffect(() => {
        const fetchSites = async () => {
            try {
                const response = await axios.get("http://localhost:8080/api/v1/crawler/getAllSitesStats", {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem("token")}`,
                    },
                });

                const sitesData = response.data.map((site: any) => ({
                    ...site,
                    lastScraped: site.lastScraped || "Не указано",
                    pagesCount: site.pagesCount || 0,
                }));
                setSites(sitesData);
            } catch (error) {
                console.error("Ошибка при получении сайтов", error);
            }
        };

        fetchSites();
    }, []);

    const handleRescrape = async (siteUrl: string) => {
        try {
            const response = await fetch(`http://localhost:8080/api/v1/crawler?url=${encodeURIComponent(siteUrl)}`, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${localStorage.getItem("token")}`,
                },
            });

            if (response.ok) {
                alert("Сайт обновляется");
            } else {
                alert("Ошибка при повторном скрапинге");
            }
        } catch (error) {
            console.error("Ошибка при повторном скрапинге", error);
        }
    };

    const handleStartScraping = async () => {
        if (url) {
            try {
                const response = await fetch(`http://localhost:8080/api/v1/crawler?url=${encodeURIComponent(url)}`, {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem("token")}`,
                    },
                });

                if (response.ok) {
                    alert("Скрапинг начат!");
                    setOpenDrawer(false);
                } else {
                    alert("Ошибка при запуске скрапинга");
                }
            } catch (error) {
                console.error("Ошибка при запуске скрапинга", error);
            }
        } else {
            alert("Пожалуйста, введите URL");
        }
    };

    const handleAnalyzeShops = () => {
        navigate("/analyze-shops");
        setOpenDrawer(false);
    };

    const handleAnalyzeBlogs = () => {
        navigate("/analyze-blogs");
        setOpenDrawer(false);
    };

    function isTokenInvalid(token: string | null): boolean {
        return !token || token === "null" || token === "undefined";
    }

    interface User {
        id: string;
        name: string;
        email: string;
    }

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh" bgcolor="#f5f7fa">
                <CircularProgress/>
            </Box>
        );
    }

    return (
        <>
            {/* Верхний навигационный бар */}
            <AppBar position="static" color="default" elevation={0} sx={{bgcolor: 'white'}}>
                <Toolbar>
                    <IconButton
                        edge="start"
                        color="inherit"
                        onClick={() => setOpenDrawer(true)}
                        sx={{mr: 2}}
                    >
                        <MenuIcon/>
                    </IconButton>

                    <Typography variant="h6" component="div" sx={{flexGrow: 1}}>
                        👋 Добро пожаловать, <strong>{user?.name}</strong>!
                    </Typography>

                    <Typography variant="subtitle1" sx={{mr: 2}}>
                        {user?.email}
                    </Typography>

                    <Button
                        variant="outlined"
                        color="error"
                        onClick={handleLogout}
                        sx={{ml: 2}}
                    >
                        Выйти
                    </Button>
                </Toolbar>
            </AppBar>

            <Container sx={{mt: 4}}>
                {sites.length === 0 ? (
                    <Typography variant="h6" align="center" color="textSecondary">
                        Нет данных для отображения.
                    </Typography>
                ) : (
                    sites.map((site) => (
                        <SiteCard
                            key={site.id}
                            site={site}
                            onRescrape={() => handleRescrape(site.url)}
                        />
                    ))
                )}
            </Container>

            <Drawer
                anchor="left"
                open={openDrawer}
                onClose={() => setOpenDrawer(false)}
                sx={{
                    "& .MuiDrawer-paper": {
                        width: 250,
                        padding: 2,
                        paddingTop: 4,
                    },
                }}
            >
                <Stack spacing={2}>
                    <Typography variant="h6">
                        Скрапинг сайта
                    </Typography>

                    <TextField
                        fullWidth
                        variant="outlined"
                        label="URL сайта"
                        value={url}
                        onChange={(e) => setUrl(e.target.value)}
                    />

                    <Button
                        variant="contained"
                        color="primary"
                        onClick={handleStartScraping}
                        sx={{width: "100%"}}
                    >
                        Начать скрапинг
                    </Button>

                    {/* Разделитель */}
                    <Divider sx={{my: 2}}/>

                    <Typography variant="h6">
                        Анализ сайтов
                    </Typography>

                    {/* Кнопки для анализа теперь под кнопкой скрапинга */}
                    <Button
                        variant="outlined"
                        startIcon={<StoreIcon/>}
                        onClick={handleAnalyzeShops}
                        sx={{justifyContent: 'flex-start'}}
                    >
                        Анализ магазинов
                    </Button>

                    <Button
                        variant="outlined"
                        startIcon={<ArticleIcon/>}
                        onClick={handleAnalyzeBlogs}
                        sx={{justifyContent: 'flex-start'}}
                    >
                        Анализ статей
                    </Button>
                </Stack>
            </Drawer>
        </>
    );
}