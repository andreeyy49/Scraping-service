import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import axios from "axios";
import {
    Container,
    Typography,
    CircularProgress,
    Accordion,
    AccordionSummary,
    AccordionDetails,
    Grid,
    Card,
    CardContent,
    Box,
    Pagination,
    Button,
    Link,
    AppBar,
    Toolbar,
    IconButton,
    Drawer,
    Stack,
    Divider,
    Chip,
    List,
    ListItem,
    ListItemText,
    Avatar,
    ListItemAvatar
} from "@mui/material";
import {
    Menu as MenuIcon,
    Store as StoreIcon,
    Article as ArticleIcon,
    Logout as LogoutIcon,
    ExpandMore as ExpandMoreIcon,
    ArrowBack as ArrowBackIcon,
    Link as LinkIcon,
    Schedule as ScheduleIcon,
    NewReleases as NewReleasesIcon,
    Delete as DeleteIcon
} from "@mui/icons-material";

interface BlogDto {
    blogText: string;
    path: string;
    parseTime: string;
    siteId: number;
    keyWords: string[];
}

interface BlogTextChangeDto {
    blogs: BlogDto[];
}

interface PageData<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
}

interface GroupedBlogs {
    [path: string]: BlogDto[];
}

export default function BlogStatsPage() {
    const { siteId } = useParams();
    const navigate = useNavigate();
    const [newBlogs, setNewBlogs] = useState<PageData<BlogDto> | null>(null);
    const [oldBlogs, setOldBlogs] = useState<PageData<BlogDto> | null>(null);
    const [changedBlogs, setChangedBlogs] = useState<PageData<BlogTextChangeDto> | null>(null);
    const [loading, setLoading] = useState({
        new: true,
        old: true,
        changed: true
    });
    const [openDrawer, setOpenDrawer] = useState(false);
    const [expanded, setExpanded] = useState<string | false>('panel1');
    const [newPage, setNewPage] = useState(1);
    const [oldPage, setOldPage] = useState(1);
    const [changedPage, setChangedPage] = useState(1);
    const itemsPerPage = 5;

    const groupBlogsByPath = (blogGroups: BlogTextChangeDto[]): GroupedBlogs => {
        const grouped: GroupedBlogs = {};

        blogGroups.forEach(group => {
            group.blogs.forEach(blog => {
                if (!grouped[blog.path]) {
                    grouped[blog.path] = [];
                }
                grouped[blog.path].push(blog);
            });
        });

        Object.keys(grouped).forEach(path => {
            grouped[path].sort((a, b) =>
                new Date(b.parseTime).getTime() - new Date(a.parseTime).getTime()
            );
        });

        return grouped;
    };

    const handleAccordionChange = (panel: string) => (_: React.SyntheticEvent, isExpanded: boolean) => {
        setExpanded(isExpanded ? panel : false);
    };

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const token = localStorage.getItem("token");

                const requests = [
                    axios.get(`http://localhost:8080/api/v1/entity-vault/blog/findNew/${siteId}`, {
                        headers: { Authorization: `Bearer ${token}` },
                        params: { page: newPage - 1, size: itemsPerPage }
                    }).then(res => {
                        setNewBlogs(res.data);
                        setLoading(prev => ({ ...prev, new: false }));
                    }),

                    axios.get(`http://localhost:8080/api/v1/entity-vault/blog/findOld/${siteId}`, {
                        headers: { Authorization: `Bearer ${token}` },
                        params: { page: oldPage - 1, size: itemsPerPage }
                    }).then(res => {
                        setOldBlogs(res.data);
                        setLoading(prev => ({ ...prev, old: false }));
                    }),

                    axios.get(`http://localhost:8080/api/v1/entity-vault/blog/findChangedBlogs/${siteId}`, {
                        headers: { Authorization: `Bearer ${token}` },
                        params: { page: changedPage - 1, size: itemsPerPage }
                    }).then(res => {
                        setChangedBlogs(res.data);
                        setLoading(prev => ({ ...prev, changed: false }));
                    })
                ];

                await Promise.all(requests);
            } catch (err) {
                console.error("Ошибка при загрузке статистики блогов", err);
            }
        };

        fetchStats();
    }, [siteId, newPage, oldPage, changedPage]);

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

    if (loading.new && loading.old && loading.changed && !newBlogs && !oldBlogs && !changedBlogs) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
                <CircularProgress />
            </Box>
        );
    }

    return (
        <>
            <AppBar position="static" color="default" elevation={0} sx={{ bgcolor: 'white' }}>
                <Toolbar>
                    <IconButton edge="start" color="inherit" onClick={() => setOpenDrawer(true)} sx={{ mr: 2 }}>
                        <MenuIcon />
                    </IconButton>

                    <IconButton edge="start" color="inherit" onClick={() => navigate(-1)} sx={{ mr: 2 }}>
                        <ArrowBackIcon />
                    </IconButton>

                    <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                        Статистика по блогам сайта #{siteId}
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
                {/* Аккордеон для новых статей */}
                <Accordion
                    expanded={expanded === 'panel1'}
                    onChange={handleAccordionChange('panel1')}
                    sx={{ mb: 3 }}
                >
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography variant="h6" sx={{ fontWeight: 'bold', display: 'flex', alignItems: 'center' }}>
                            <NewReleasesIcon color="primary" sx={{ mr: 1, fontSize: '1.5rem' }} />
                            Новые статьи ({newBlogs?.totalElements ?? 0})
                        </Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        <BlogList
                            blogs={newBlogs?.content ?? []}
                            page={newPage}
                            totalPages={newBlogs?.totalPages ?? 1}
                            onPageChange={(_, val) => setNewPage(val)}
                            loading={loading.new}
                            emptyMessage="Новых статей не найдено"
                        />
                    </AccordionDetails>
                </Accordion>

                {/* Аккордеон для пропавших статей */}
                <Accordion
                    expanded={expanded === 'panel2'}
                    onChange={handleAccordionChange('panel2')}
                    sx={{ mb: 3 }}
                >
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography variant="h6" sx={{ fontWeight: 'bold', display: 'flex', alignItems: 'center' }}>
                            <DeleteIcon color="error" sx={{ mr: 1, fontSize: '1.5rem' }} />
                            Пропавшие статьи ({oldBlogs?.totalElements ?? 0})
                        </Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        <BlogList
                            blogs={oldBlogs?.content ?? []}
                            page={oldPage}
                            totalPages={oldBlogs?.totalPages ?? 1}
                            onPageChange={(_, val) => setOldPage(val)}
                            loading={loading.old}
                            emptyMessage="Пропавших статей не найдено"
                        />
                    </AccordionDetails>
                </Accordion>

                {/* Аккордеон для измененных статей */}
                <Accordion
                    expanded={expanded === 'panel3'}
                    onChange={handleAccordionChange('panel3')}
                >
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography variant="h6" sx={{ fontWeight: 'bold', display: 'flex', alignItems: 'center' }}>
                            <ScheduleIcon color="action" sx={{ mr: 1, fontSize: '1.5rem' }} />
                            Измененные статьи ({changedBlogs?.totalElements ?? 0})
                        </Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        <BlogChangesGrid
                            blogGroups={changedBlogs?.content ?? []}
                            page={changedPage}
                            totalPages={changedBlogs?.totalPages ?? 1}
                            onPageChange={(_, val) => setChangedPage(val)}
                            loading={loading.changed}
                            groupBlogsByPath={groupBlogsByPath}
                        />
                    </AccordionDetails>
                </Accordion>
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

function BlogList({
                      blogs,
                      page,
                      totalPages,
                      onPageChange,
                      loading,
                      emptyMessage
                  }: {
    blogs: BlogDto[],
    page: number,
    totalPages: number,
    onPageChange: (event: React.ChangeEvent<unknown>, value: number) => void,
    loading: boolean,
    emptyMessage: string
}) {
    if (loading) {
        return (
            <Box display="flex" justifyContent="center" my={4}>
                <CircularProgress />
            </Box>
        );
    }

    if (blogs.length === 0) {
        return <Typography variant="body1" textAlign="center" sx={{ mt: 2 }}>{emptyMessage}</Typography>;
    }

    return (
        <>
            <Box sx={{ display: 'flex', justifyContent: 'center', mb: 3 }}>
                <Pagination
                    count={totalPages}
                    page={page}
                    onChange={onPageChange}
                    color="primary"
                    showFirstButton
                    showLastButton
                />
            </Box>

            <Grid container spacing={3}>
                {blogs.map((blog) => (
                    <Grid item xs={12} key={blog.path}>
                        <Card variant="outlined">
                            <CardContent>
                                <Typography variant="h6" gutterBottom>
                                    <Link href={blog.path} target="_blank" rel="noopener" sx={{ textDecoration: 'none' }}>
                                        <Button startIcon={<LinkIcon color="primary" />} color="primary">
                                            {blog.path}
                                        </Button>
                                    </Link>
                                </Typography>

                                <Box display="flex" alignItems="center" mt={1} mb={1}>
                                    <ScheduleIcon color="action" fontSize="small" sx={{ mr: 1 }} />
                                    <Typography variant="body2" color="text.secondary">
                                        {new Date(blog.parseTime).toLocaleString()}
                                    </Typography>
                                </Box>

                                {blog.keyWords && blog.keyWords.length > 0 && (
                                    <Box mt={2}>
                                        <Typography variant="subtitle2">Ключевые слова:</Typography>
                                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                                            {blog.keyWords.map((word, i) => (
                                                <Chip key={i} label={word} size="small" color="primary" />
                                            ))}
                                        </Box>
                                    </Box>
                                )}
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </>
    );
}

function BlogChangesGrid({
                             blogGroups,
                             page,
                             totalPages,
                             onPageChange,
                             loading,
                             groupBlogsByPath
                         }: {
    blogGroups: BlogTextChangeDto[],
    page: number,
    totalPages: number,
    onPageChange: (event: React.ChangeEvent<unknown>, value: number) => void,
    loading: boolean,
    groupBlogsByPath: (groups: BlogTextChangeDto[]) => GroupedBlogs
}) {
    const groupedBlogs = groupBlogsByPath(blogGroups);

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" my={4}>
                <CircularProgress />
            </Box>
        );
    }

    if (Object.keys(groupedBlogs).length === 0) {
        return <Typography variant="body1" textAlign="center" sx={{ mt: 2 }}>Нет измененных статей</Typography>;
    }

    return (
        <>
            <Box sx={{ display: 'flex', justifyContent: 'center', mb: 3 }}>
                <Pagination
                    count={totalPages}
                    page={page}
                    onChange={onPageChange}
                    color="primary"
                    showFirstButton
                    showLastButton
                />
            </Box>

            <Grid container spacing={3}>
                {Object.entries(groupedBlogs).map(([path, blogs]) => (
                    <Grid item xs={12} key={path}>
                        <Card variant="outlined">
                            <CardContent>
                                <Typography variant="h6" gutterBottom>
                                    <Link href={path} target="_blank" rel="noopener" sx={{ textDecoration: 'none' }}>
                                        <Button startIcon={<LinkIcon color="primary" />} color="primary">
                                            {path}
                                        </Button>
                                    </Link>
                                </Typography>

                                <Accordion defaultExpanded variant="outlined">
                                    <AccordionSummary expandIcon={<ExpandMoreIcon color="primary" />}>
                                        <Typography>История изменений ({blogs.length} версий)</Typography>
                                    </AccordionSummary>
                                    <AccordionDetails>
                                        <List>
                                            {blogs.map((blog, index) => (
                                                <ListItem key={`${path}-${index}`} alignItems="flex-start">
                                                    <ListItemAvatar>
                                                        <Avatar sx={{ bgcolor: 'primary.main' }}>
                                                            <ScheduleIcon />
                                                        </Avatar>
                                                    </ListItemAvatar>
                                                    <ListItemText
                                                        primary={new Date(blog.parseTime).toLocaleString()}
                                                        secondary={
                                                            blog.keyWords && blog.keyWords.length > 0 && (
                                                                <Box mt={1}>
                                                                    <Typography variant="subtitle2">Ключевые слова:</Typography>
                                                                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                                                                        {blog.keyWords.map((word, i) => (
                                                                            <Chip key={i} label={word} size="small" color="primary" />
                                                                        ))}
                                                                    </Box>
                                                                </Box>
                                                            )
                                                        }
                                                    />
                                                </ListItem>
                                            ))}
                                        </List>
                                    </AccordionDetails>
                                </Accordion>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </>
    );
}