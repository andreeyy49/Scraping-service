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
    Collapse,
    Chip,
    MenuItem,
    Pagination
} from "@mui/material";
import {
    Menu as MenuIcon,
    Store as StoreIcon,
    Article as ArticleIcon,
    Logout as LogoutIcon,
    ExpandMore,
    ExpandLess
} from "@mui/icons-material";
import { useNavigate } from "react-router-dom";
import axios from "axios";

interface Blog {
    id: string;
    blogText: string;
    path: string;
    parseTime: string;
    siteId: number;
    keyWords: string[];
    images: string[];
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

export default function AnalyzeBlogsPage() {
    const [keywords, setKeywords] = useState("");
    const [blogs, setBlogs] = useState<Blog[]>([]);
    const [loading, setLoading] = useState(false);
    const [expandedId, setExpandedId] = useState<string | null>(null);
    const [openDrawer, setOpenDrawer] = useState(false);
    const [sites, setSites] = useState<Site[]>([]);
    const [selectedSite, setSelectedSite] = useState("");
    const [page, setPage] = useState(1);
    const [size, setSize] = useState(5);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [categories, setCategories] = useState<string[]>([]);
    const [selectedCategory, setSelectedCategory] = useState<string | null>(null);

    const navigate = useNavigate();

    useEffect(() => {
        const fetchCategories = async () => {
            try {
                const res = await axios.get("http://localhost:8080/api/v1/entity-vault/blog/allCategory", {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem("token")}`,
                    },
                });
                setCategories(res.data);
            } catch (e) {
                console.error("Ошибка при загрузке категорий", e);
            }
        };

        fetchCategories();
    }, []);

    const handleSearchByCategory = async (category: string, newPage = 1, newSize = size) => {
        setSelectedCategory(category);
        setKeywords(category); // Устанавливаем категорию как ключевое слово для отображения
        setLoading(true);
        try {
            let url;
            const keywordsArray = [category.trim()];
            if (selectedSite) {
                // Поиск по категории и сайту
                url = `http://localhost:8080/api/v1/entity-vault/blog/findByKeyWordsAndSiteId/${selectedSite}?page=${newPage - 1}&size=${newSize}`;
            } else {
                // Поиск только по категории
                url = `http://localhost:8080/api/v1/entity-vault/blog/findByKeyWords?page=${newPage - 1}&size=${newSize}`;
            }

            const response = await axios.post<PaginatedResponse<Blog>>(url, keywordsArray, {
                headers: {
                    Authorization: `Bearer ${localStorage.getItem("token")}`,
                },
            });

            setBlogs(response.data.content);
            setTotalPages(response.data.totalPages);
            setTotalElements(response.data.totalElements);
            setPage(newPage);
            setSize(newSize);
        } catch (error) {
            console.error("Ошибка при поиске блогов по категории:", error);
            alert("Произошла ошибка при поиске блогов по категории");
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = async (newPage = 1, newSize = size) => {
        if (!keywords.trim()) return;

        setSelectedCategory(null); // Сбрасываем выбранную категорию при ручном поиске
        setLoading(true);
        try {
            let url;
            const keywordsArray = keywords.split(",").map(k => k.trim());

            if (selectedSite) {
                url = `http://localhost:8080/api/v1/entity-vault/blog/findByKeyWordsAndSiteId/${selectedSite}?page=${newPage - 1}&size=${newSize}`;
                const response = await axios.post<PaginatedResponse<Blog>>(url, keywordsArray, {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem("token")}`,
                    },
                });
                setBlogs(response.data.content);
                setTotalPages(response.data.totalPages);
                setTotalElements(response.data.totalElements);
            } else {
                url = `http://localhost:8080/api/v1/entity-vault/blog/findByKeyWords?page=${newPage - 1}&size=${newSize}`;
                const response = await axios.post<PaginatedResponse<Blog>>(url, keywordsArray, {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem("token")}`,
                    },
                });
                setBlogs(response.data.content);
                setTotalPages(response.data.totalPages);
                setTotalElements(response.data.totalElements);
            }
            setPage(newPage);
            setSize(newSize);
        } catch (error) {
            console.error("Ошибка при поиске блогов:", error);
            alert("Произошла ошибка при поиске блогов");
        } finally {
            setLoading(false);
        }
    };

    const handlePageChange = (event: React.ChangeEvent<unknown>, newPage: number) => {
        if (selectedCategory) {
            handleSearchByCategory(selectedCategory, newPage);
        } else {
            handleSearch(newPage);
        }
    };

    const handleSiteChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedSite(event.target.value);
        setPage(1);
        if (selectedCategory) {
            // Если выбрана категория, выполняем поиск по новой категории и сайту
            handleSearchByCategory(selectedCategory, 1);
        }
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
                const res = await axios.get("http://localhost:8080/api/v1/crawler/blogs", {
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
                        Анализ статей и блогов
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
                        label="Ключевые слова (через запятую)"
                        value={keywords}
                        onChange={(e) => setKeywords(e.target.value)}
                        onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                        placeholder="Например: технологии, искусственный интеллект, программирование"
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
                        disabled={loading || !keywords.trim()}
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

                {blogs.length > 0 && (
                    <>
                        <Grid container spacing={3}>
                            {blogs.map((blog) => (
                                <Grid item xs={12} key={blog.id}>
                                    <Card sx={{
                                        display: 'flex',
                                        flexDirection: 'column',
                                        '&:hover': {
                                            boxShadow: '0px 4px 20px rgba(0, 0, 0, 0.1)'
                                        }
                                    }}>
                                        <CardContent>
                                            <Box sx={{
                                                display: 'flex',
                                                justifyContent: 'space-between',
                                                alignItems: 'flex-start',
                                                mb: 1
                                            }}>
                                                <Box>
                                                    <Typography variant="subtitle2" color="text.secondary">
                                                        {new Date(blog.parseTime).toLocaleString()}
                                                    </Typography>
                                                    <Box sx={{ mt: 1 }}>
                                                        {blog.keyWords.slice(0, 5).map((keyword, index) => (
                                                            <Chip
                                                                key={index}
                                                                label={keyword}
                                                                size="small"
                                                                sx={{ mr: 0.5, mb: 0.5 }}
                                                            />
                                                        ))}
                                                    </Box>
                                                </Box>
                                                <IconButton
                                                    onClick={() => setExpandedId(expandedId === blog.id ? null : blog.id)}
                                                    aria-label={expandedId === blog.id ? "Свернуть" : "Развернуть"}
                                                    size="small"
                                                >
                                                    {expandedId === blog.id ? <ExpandLess /> : <ExpandMore />}
                                                </IconButton>
                                            </Box>

                                            <Collapse in={expandedId === blog.id} collapsedSize={120}>
                                                <Typography
                                                    variant="body1"
                                                    paragraph
                                                    sx={{
                                                        whiteSpace: 'pre-line',
                                                        lineHeight: 1.6,
                                                        textAlign: 'justify',
                                                        mt: 2
                                                    }}
                                                >
                                                    {blog.blogText}
                                                </Typography>
                                                {expandedId === blog.id && blog.blogText.length > 1000 && (
                                                    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
                                                        <Button
                                                            startIcon={<ExpandLess />}
                                                            onClick={() => setExpandedId(null)}
                                                            size="small"
                                                            sx={{ mt: 1 }}
                                                        >
                                                            Свернуть статью
                                                        </Button>
                                                    </Box>
                                                )}
                                            </Collapse>
                                        </CardContent>

                                        {blog.images.length > 0 && (
                                            <Box sx={{
                                                p: 2,
                                                display: 'flex',
                                                gap: 1,
                                                overflowX: 'auto',
                                                borderTop: '1px solid',
                                                borderColor: 'divider'
                                            }}>
                                                {blog.images.map((img, index) => (
                                                    <CardMedia
                                                        key={index}
                                                        component="img"
                                                        image={img}
                                                        alt={`Изображение ${index + 1}`}
                                                        sx={{
                                                            width: 120,
                                                            height: 90,
                                                            objectFit: 'cover',
                                                            borderRadius: 1,
                                                            border: '1px solid #eee',
                                                        }}
                                                    />
                                                ))}
                                            </Box>
                                        )}

                                        <Box sx={{ p: 2 }}>
                                            <Link
                                                href={blog.path}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                sx={{ textDecoration: 'none' }}
                                            >
                                                <Button
                                                    fullWidth
                                                    variant="contained"
                                                    size="small"
                                                    sx={{ textTransform: 'none' }}
                                                >
                                                    Перейти к оригинальной статье
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
                            Всего статей: {totalElements}
                        </Typography>
                    </>
                )}

                {!loading && blogs.length === 0 && keywords && (
                    <Typography variant="body1" textAlign="center" sx={{ mt: 4, color: 'text.secondary' }}>
                        По вашему запросу статьи не найдены. Попробуйте изменить ключевые слова.
                    </Typography>
                )}
            </Container>

            <Box sx={{
                display: 'flex',
                justifyContent: 'center',
                overflowX: 'auto',
                pb: 2,
                mb: 2,
                gap: 1,
                maxWidth: '100%',
                flexWrap: 'nowrap',
                scrollbarWidth: 'none',
                '&::-webkit-scrollbar': { display: 'none' }
            }}>
                <Box sx={{ display: 'flex', gap: 1 }}>
                    {categories.map((category) => (
                        <Chip
                            key={category}
                            label={category}
                            clickable
                            onClick={() => handleSearchByCategory(category)}
                            sx={{ flexShrink: 0 }}
                            color={selectedCategory === category ? "primary" : "default"}
                            variant={selectedCategory === category ? "filled" : "outlined"}
                        />
                    ))}
                </Box>
            </Box>

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
                        variant="outlined"
                        startIcon={<StoreIcon />}
                        onClick={() => navigate("/analyze-shops")}
                        sx={{ justifyContent: 'flex-start' }}
                    >
                        Анализ магазинов
                    </Button>

                    <Button
                        variant="contained"
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