import { useState, useEffect } from "react";
import {
    Container,
    TextField,
    Button,
    Grid,
    Card,
    CardMedia,
    CardContent,
    Typography,
    Box,
    CircularProgress,
    Link,
    AppBar,
    Toolbar,
    IconButton,
    Drawer,
    Stack,
    Divider,
    MenuItem,
    Pagination
} from "@mui/material";
import {
    Menu as MenuIcon,
    Store as StoreIcon,
    Article as ArticleIcon,
    Logout as LogoutIcon
} from "@mui/icons-material";
import { useNavigate } from "react-router-dom";
import axios from "axios";

interface Product {
    id: string;
    name: string;
    price: string;
    imageUrl: string;
    productUrl: string;
}

interface Site {
    id: string;
    url: string;
    name: string;
    lastScraped: string;
    type: string;
    pageCount: number;
    status: "INDEXING" | "INDEXED" | "FAILED";
}

interface PaginatedResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    number: number;
    size: number;
}

export default function AnalyzeShopsPage() {
    const [query, setQuery] = useState("");
    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState(false);
    const [openDrawer, setOpenDrawer] = useState(false);
    const [sites, setSites] = useState<Site[]>([]);
    const [selectedSite, setSelectedSite] = useState("");
    const [page, setPage] = useState(1);
    const [size, setSize] = useState(9);

    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const navigate = useNavigate();

    const handleSearch = async (newPage = 1, newSize = size) => {
        if (!query.trim()) return;

        setLoading(true);
        try {
            let url;
            if (selectedSite) {
                url = `http://localhost:8080/api/v1/entity-vault/product/findAllByTitleAndSiteId/${encodeURIComponent(query)}/${selectedSite}?page=${newPage - 1}&size=${newSize}`;
            } else {
                url = `http://localhost:8080/api/v1/entity-vault/product/findAllByTitle/${encodeURIComponent(query)}?page=${newPage - 1}&size=${newSize}`;
            }

            const response = await axios.get<PaginatedResponse<Product>>(url, {
                headers: {
                    Authorization: `Bearer ${localStorage.getItem("token")}`,
                },
            });

            setProducts(response.data.content);
            setTotalPages(response.data.totalPages);
            setTotalElements(response.data.totalElements);
            setPage(newPage);
            setSize(newSize);
        } catch (error) {
            console.error("Ошибка при поиске товаров", error);
            alert("Произошла ошибка при поиске товаров");
        } finally {
            setLoading(false);
        }
    };

    const handlePageChange = (event: React.ChangeEvent<unknown>, newPage: number) => {
        handleSearch(newPage);
    };

    const handleSiteChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedSite(event.target.value);
        setPage(1); // Сброс пагинации при изменении сайта
    };

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
        const fetchSites = async () => {
            try {
                const res = await axios.get("http://localhost:8080/api/v1/crawler/e-comers", {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem("token")}`,
                    },
                });
                setSites(res.data);
            } catch (e) {
                console.error("Ошибка при загрузке сайтов", e);
            }
        };

        fetchSites();
    }, []);

    return (
        <>
            <AppBar position="static" color="default" elevation={0} sx={{ bgcolor: 'white' }}>
                <Toolbar>
                    <IconButton
                        edge="start"
                        color="inherit"
                        onClick={() => setOpenDrawer(true)}
                        sx={{ mr: 2 }}
                    >
                        <MenuIcon />
                    </IconButton>

                    <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                        Анализ магазинов
                    </Typography>

                    <Button
                        variant="outlined"
                        color="error"
                        onClick={handleLogout}
                        startIcon={<LogoutIcon />}
                        sx={{ ml: 2 }}
                    >
                        Выйти
                    </Button>
                </Toolbar>
            </AppBar>

            <Container sx={{ mt: 4 }}>
                <Box sx={{ display: 'flex', gap: 2, mb: 4, flexWrap: 'wrap' }}>
                    <TextField
                        fullWidth
                        variant="outlined"
                        label="Поиск товаров"
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                        onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                        sx={{ flex: 2, minWidth: 250 }}
                    />

                    <TextField
                        select
                        fullWidth
                        label="Выберите сайт"
                        value={selectedSite}
                        onChange={handleSiteChange}
                        sx={{ flex: 1, minWidth: 180 }}
                    >
                        <MenuItem value="">Все сайты</MenuItem>
                        {sites.map((site) => (
                            <MenuItem key={site.id} value={site.id}>
                                {site.name}
                            </MenuItem>
                        ))}
                    </TextField>

                    <Button
                        variant="contained"
                        onClick={() => handleSearch()}
                        disabled={loading || !query.trim()}
                        sx={{ minWidth: 120 }}
                    >
                        {loading ? <CircularProgress size={24} /> : "Найти"}
                    </Button>

                    <Button
                        variant="outlined"
                        color="primary"
                        onClick={() => navigate(`/site-stats/${selectedSite}`)}
                        disabled={!selectedSite}
                        sx={{ minWidth: 180 }}
                    >
                        Статистика по сайту
                    </Button>
                </Box>

                {loading && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                        <CircularProgress />
                    </Box>
                )}

                {products.length > 0 && (
                    <>
                        <Grid container spacing={3}>
                            {products.map((product) => (
                                <Grid item xs={12} sm={6} md={4} key={product.id}>
                                    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                                        <CardMedia
                                            component="img"
                                            height="200"
                                            image={product.imageUrl || "https://via.placeholder.com/300"}
                                            alt={product.name}
                                            sx={{ objectFit: 'contain', p: 1 }}
                                        />

                                        <CardContent sx={{ flexGrow: 1 }}>
                                            <Typography gutterBottom variant="h6" component="div">
                                                {product.name}
                                            </Typography>

                                            <Typography variant="h5" color="primary" sx={{ fontWeight: 'bold' }}>
                                                {product.price}
                                            </Typography>
                                        </CardContent>

                                        <Box sx={{ p: 2 }}>
                                            <Link
                                                href={product.productUrl}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                sx={{ textDecoration: 'none' }}
                                            >
                                                <Button fullWidth variant="contained">
                                                    Перейти к товару
                                                </Button>
                                            </Link>
                                        </Box>
                                    </Card>
                                </Grid>
                            ))}
                        </Grid>

                        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                            <Pagination
                                count={totalPages}
                                page={page}
                                onChange={handlePageChange}
                                color="primary"
                                showFirstButton
                                showLastButton
                            />
                        </Box>

                        <Typography variant="body2" color="text.secondary" textAlign="center" mt={1}>
                            Всего товаров: {totalElements}
                        </Typography>
                    </>
                )}

                {!loading && products.length === 0 && query && (
                    <Typography variant="body1" textAlign="center" sx={{ mt: 4 }}>
                        Товары не найдены. Попробуйте изменить запрос.
                    </Typography>
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
                        Навигация
                    </Typography>

                    <Button
                        variant="outlined"
                        startIcon={<StoreIcon />}
                        onClick={() => navigate("/dashboard")}
                        sx={{ justifyContent: 'flex-start' }}
                    >
                        Дашборд
                    </Button>

                    <Button
                        variant="contained"
                        startIcon={<StoreIcon />}
                        onClick={() => navigate("/analyze-shops")}
                        sx={{ justifyContent: 'flex-start' }}
                    >
                        Анализ магазинов
                    </Button>

                    <Button
                        variant="outlined"
                        startIcon={<ArticleIcon />}
                        onClick={() => navigate("/analyze-blogs")}
                        sx={{ justifyContent: 'flex-start' }}
                    >
                        Анализ статей
                    </Button>

                    <Divider sx={{ my: 2 }} />

                    <Button
                        variant="outlined"
                        color="error"
                        startIcon={<LogoutIcon />}
                        onClick={handleLogout}
                        sx={{ justifyContent: 'flex-start' }}
                    >
                        Выйти
                    </Button>
                </Stack>
            </Drawer>
        </>
    );
}