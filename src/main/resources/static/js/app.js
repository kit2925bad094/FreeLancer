/**
 * FreelanceHub - Modern SaaS Frontend Application Script
 * Full-Stack End-to-End REST integration with Spring Boot & JDBC
 */

const API_BASE = (typeof window !== 'undefined' && (window.FH_API_BASE || localStorage.getItem('fh_api_base'))) || '/api';

class FreelanceHubApp {
    constructor() {
        this.currentUser = JSON.parse(localStorage.getItem('fh_user')) || null;
        this.currentTheme = localStorage.getItem('fh_theme') || 'dark';
        this.activePage = 'home';
        this.searchDebounceTimer = null;
        this.currentProjectId = null;
        this.chatPollInterval = null;
        this.charts = {};
        this.availableSkills = [];
        this.selectedRegSkills = new Set();
        this.selectedProjectSkills = new Set();
        this.loginRole = 'FREELANCER';

        this.init();
    }

    init() {
        // Theme initialization
        this.applyTheme(this.currentTheme);
        document.getElementById('theme-toggle-btn').addEventListener('click', () => this.toggleTheme());

        // Navigation hash listener
        window.addEventListener('hashchange', () => this.handleRouting());

        // Dropdown toggle listeners
        this.initDropdowns();

        // Star rating picker listener
        this.initStarRating();

        // Initial setup
        this.updateAuthUI();
        this.handleRouting();
        this.loadSkillsCatalog();
        this.loadSkillsFilter();

        // If user logged in, start notification polling
        if (this.currentUser) {
            this.loadNotificationCount();
            setInterval(() => this.loadNotificationCount(), 15000);
        }
    }

    // ========================================================
    // THEME HANDLING
    // ========================================================
    applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        const icon = document.getElementById('theme-icon');
        if (theme === 'dark') {
            icon.className = 'fa-solid fa-sun';
        } else {
            icon.className = 'fa-solid fa-moon';
        }
        localStorage.setItem('fh_theme', theme);
        this.currentTheme = theme;
    }

    toggleTheme() {
        const next = this.currentTheme === 'dark' ? 'light' : 'dark';
        this.applyTheme(next);
    }

    // ========================================================
    // ROUTING & NAVIGATION
    // ========================================================
    handleRouting() {
        const hash = window.location.hash.slice(1) || 'home';
        const parts = hash.split('?');
        const route = parts[0];
        const params = new URLSearchParams(parts[1] || '');

        this.navigate(route, params, false);
    }

    navigate(page, params = new URLSearchParams(), updateHash = true) {
        this.activePage = page;
        if (updateHash) {
            const queryStr = params.toString() ? '?' + params.toString() : '';
            window.location.hash = page + queryStr;
        }

        // Update nav active link
        document.querySelectorAll('.nav-link').forEach(link => {
            if (link.dataset.page === page || (page === 'dashboard' && link.dataset.page === 'dashboard')) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });

        // Hide all views
        document.querySelectorAll('.page-view').forEach(view => view.style.display = 'none');

        // Close any chat polling if leaving project details
        if (page !== 'project-details' && this.chatPollInterval) {
            clearInterval(this.chatPollInterval);
            this.chatPollInterval = null;
        }

        // Show selected view
        window.scrollTo({ top: 0, behavior: 'smooth' });

        switch (page) {
            case 'home':
                document.getElementById('home-view').style.display = 'block';
                this.loadHomeStats();
                this.loadFeaturedProjects();
                break;
            case 'projects':
                document.getElementById('projects-view').style.display = 'block';
                this.loadProjects();
                break;
            case 'project-details':
                document.getElementById('project-details-view').style.display = 'block';
                const pId = params.get('id');
                if (pId) {
                    this.loadProjectDetails(pId);
                } else {
                    this.navigate('projects');
                }
                break;
            case 'freelancers':
                document.getElementById('freelancers-view').style.display = 'block';
                this.loadFreelancers();
                break;
            case 'dashboard':
                this.goToMyDashboard();
                break;
            case 'freelancer-dashboard':
                document.getElementById('freelancer-dashboard-view').style.display = 'block';
                this.loadFreelancerDashboard();
                break;
            case 'client-dashboard':
                document.getElementById('client-dashboard-view').style.display = 'block';
                this.loadClientDashboard();
                break;
            case 'admin-dashboard':
                document.getElementById('admin-dashboard-view').style.display = 'block';
                this.loadAdminDashboard();
                break;
            case 'profile':
                document.getElementById('profile-view').style.display = 'block';
                const uId = params.get('id') || (this.currentUser ? this.currentUser.userId : null);
                if (uId) {
                    this.loadUserProfile(uId);
                } else {
                    this.showToast('Please sign in to view profile', 'warning');
                    this.showModal('login-modal');
                }
                break;
            default:
                document.getElementById('home-view').style.display = 'block';
                this.loadHomeStats();
        }
    }

    goToMyDashboard() {
        if (!this.currentUser) {
            this.showToast('Please sign in to access your dashboard', 'info');
            this.showModal('login-modal');
            return;
        }

        if (this.currentUser.role === 'FREELANCER') {
            this.navigate('freelancer-dashboard');
        } else if (this.currentUser.role === 'CLIENT') {
            this.navigate('client-dashboard');
        } else if (this.currentUser.role === 'ADMIN') {
            this.navigate('admin-dashboard');
        }
    }

    viewMyProfile() {
        if (!this.currentUser) return;
        this.navigate('profile', new URLSearchParams({ id: this.currentUser.userId }));
    }

    // ========================================================
    // AUTHENTICATION & SESSIONS
    // ========================================================
    updateAuthUI() {
        const authBtns = document.getElementById('auth-buttons-container');
        const userMenu = document.getElementById('user-menu-container');
        const navDashLi = document.getElementById('nav-dashboard-li');
        const navPostProjBtn = document.getElementById('nav-post-project-btn');
        const notifWrapper = document.getElementById('notif-wrapper');

        if (this.currentUser) {
            if (authBtns) authBtns.style.display = 'none';
            if (userMenu) userMenu.style.display = 'block';
            if (navDashLi) navDashLi.style.display = 'block';
            if (notifWrapper) notifWrapper.style.display = 'block';

            const nameEl = document.getElementById('nav-user-name');
            if (nameEl) nameEl.textContent = this.currentUser.name;

            const roleBadge = document.getElementById('nav-user-role');
            if (roleBadge) {
                roleBadge.textContent = this.currentUser.role;
                roleBadge.className = 'user-role-badge badge-' + this.currentUser.role.toLowerCase();
            }

            const avatarEl = document.getElementById('nav-user-avatar');
            if (avatarEl) {
                if (this.currentUser.profileImage) {
                    avatarEl.src = this.currentUser.profileImage;
                    avatarEl.style.display = '';
                } else {
                    avatarEl.removeAttribute('src');
                    avatarEl.style.display = 'none';
                }
            }

            if (navPostProjBtn) {
                navPostProjBtn.style.display = this.currentUser.role === 'CLIENT' ? 'block' : 'none';
            }
        } else {
            if (authBtns) authBtns.style.display = 'flex';
            if (userMenu) userMenu.style.display = 'none';
            if (navDashLi) navDashLi.style.display = 'none';
            if (notifWrapper) notifWrapper.style.display = 'none';
            if (navPostProjBtn) navPostProjBtn.style.display = 'none';
        }
    }

    // Role-based Login Modal Controls
    openLoginModal(role = 'FREELANCER') {
        this.setLoginRole(role);
        const emailInput = document.getElementById('login-email');
        const passInput = document.getElementById('login-password');
        if (emailInput) emailInput.value = '';
        if (passInput) passInput.value = '';
        this.showModal('login-modal');
    }

    setLoginRole(role) {
        this.loginRole = role;
        const flTab = document.getElementById('login-tab-freelancer');
        const clTab = document.getElementById('login-tab-client');
        const adTab = document.getElementById('login-tab-admin');
        const hintText = document.getElementById('login-role-hint-text');
        const submitBtn = document.getElementById('login-submit-btn');

        if (flTab) flTab.className = role === 'FREELANCER' ? 'btn btn-primary btn-sm' : 'btn btn-outline btn-sm';
        if (clTab) clTab.className = role === 'CLIENT' ? 'btn btn-primary btn-sm' : 'btn btn-outline btn-sm';
        if (adTab) adTab.className = role === 'ADMIN' ? 'btn btn-primary btn-sm' : 'btn btn-outline btn-sm';

        if (role === 'FREELANCER') {
            if (hintText) hintText.textContent = 'Sign in with your registered Freelancer account to browse projects & apply.';
            if (submitBtn) submitBtn.innerHTML = '<i class="fa-solid fa-right-to-bracket"></i> Sign In as Freelancer';
        } else if (role === 'CLIENT') {
            if (hintText) hintText.textContent = 'Sign in with your registered Client account to post projects & hire talent.';
            if (submitBtn) submitBtn.innerHTML = '<i class="fa-solid fa-right-to-bracket"></i> Sign In as Client';
        } else {
            if (hintText) hintText.textContent = 'Sign in with your Administrator account to view system analytics.';
            if (submitBtn) submitBtn.innerHTML = '<i class="fa-solid fa-shield"></i> Sign In as Administrator';
        }
    }

    async handleLogin(e) {
        e.preventDefault();
        const email = document.getElementById('login-email').value.trim();
        const password = document.getElementById('login-password').value;

        if (!email || !password) {
            this.showToast('Please enter your email and password.', 'warning');
            return;
        }

        try {
            const res = await this.fetchApi('/auth/login', 'POST', { email, password });
            if (res.success && res.data) {
                this.currentUser = res.data;
                localStorage.setItem('fh_user', JSON.stringify(res.data));
                this.updateAuthUI();
                this.closeModal('login-modal');
                this.showToast(`Welcome back, ${res.data.name}! Signed in as ${res.data.role}.`, 'success');
                this.goToMyDashboard();
                this.loadNotificationCount();
            } else {
                this.showToast(res.message || 'No registered account found. Please register first!', 'error');
            }
        } catch (err) {
            this.showToast(err.message || 'No account found with this email. Please register first!', 'error');
        }
    }

    // Role-based Registration Modal Controls
    openRegisterModal(role = 'FREELANCER') {
        this.setRegRole(role);
        this.selectedRegSkills.clear();
        this.renderRegSkillChips();
        const form = document.getElementById('register-form');
        if (form) form.reset();
        this.showModal('register-modal');
    }

    setRegRole(role) {
        document.getElementById('reg-role').value = role;
        const flTab = document.getElementById('reg-tab-freelancer');
        const clTab = document.getElementById('reg-tab-client');
        const flFields = document.getElementById('reg-freelancer-fields');
        const clFields = document.getElementById('reg-client-fields');
        const titleEl = document.getElementById('register-modal-title');
        const subEl = document.getElementById('register-modal-subtitle');
        const submitBtn = document.getElementById('reg-submit-btn');

        if (role === 'FREELANCER') {
            if (flTab) flTab.className = 'btn btn-primary';
            if (clTab) clTab.className = 'btn btn-outline';
            if (flFields) flFields.style.display = 'block';
            if (clFields) clFields.style.display = 'none';
            if (titleEl) titleEl.textContent = 'Register as a Freelancer';
            if (subEl) subEl.textContent = 'Showcase your skills and start bidding on high-impact projects';
            if (submitBtn) submitBtn.innerHTML = '<i class="fa-solid fa-user-check"></i> Register as Freelancer';
            this.renderRegSkillChips();
        } else {
            if (flTab) flTab.className = 'btn btn-outline';
            if (clTab) clTab.className = 'btn btn-primary';
            if (flFields) flFields.style.display = 'none';
            if (clFields) clFields.style.display = 'block';
            if (titleEl) titleEl.textContent = 'Register as a Client';
            if (subEl) subEl.textContent = 'Publish project requirements and hire verified top developers';
            if (submitBtn) submitBtn.innerHTML = '<i class="fa-solid fa-building"></i> Register as Client';
        }
    }

    async handleRegister(e) {
        e.preventDefault();
        const role = document.getElementById('reg-role').value;
        const name = document.getElementById('reg-name').value.trim();
        const email = document.getElementById('reg-email').value.trim();
        const password = document.getElementById('reg-password').value;

        const payload = { role, name, email, password };

        if (role === 'FREELANCER') {
            payload.headline = document.getElementById('reg-headline').value.trim() || 'Software Engineer';
            payload.hourlyRate = document.getElementById('reg-rate').value ? parseFloat(document.getElementById('reg-rate').value) : 1000;
            payload.location = document.getElementById('reg-location').value.trim() || 'Remote';
            
            // Collect selected skills from interactive chips + input
            const skillsInputVal = document.getElementById('reg-skills').value;
            const typedSkills = skillsInputVal ? skillsInputVal.split(',').map(s => s.trim()).filter(Boolean) : [];
            const allSelectedSkills = Array.from(new Set([...Array.from(this.selectedRegSkills), ...typedSkills]));
            payload.skillNames = allSelectedSkills;
        } else {
            payload.companyName = document.getElementById('reg-company').value.trim() || name;
            payload.website = document.getElementById('reg-website').value.trim();
            payload.location = document.getElementById('reg-client-location')?.value?.trim() || 'Remote';
            payload.companyDescription = document.getElementById('reg-company-desc')?.value?.trim() || '';
        }

        try {
            const res = await this.fetchApi('/auth/register', 'POST', payload);
            if (res.success && res.data) {
                this.currentUser = res.data;
                localStorage.setItem('fh_user', JSON.stringify(res.data));
                this.updateAuthUI();
                this.closeModal('register-modal');
                this.showToast(`Account registered successfully! Welcome, ${res.data.name}. 🎉`, 'success');
                this.goToMyDashboard();
                this.loadHomeStats();
            } else {
                this.showToast(res.message || 'Registration failed', 'error');
            }
        } catch (err) {
            this.showToast(err.message || 'Registration error. Please check your inputs.', 'error');
        }
    }

    // Skills Interactive Badges
    async loadSkillsCatalog() {
        try {
            const res = await this.fetchApi('/skills', 'GET');
            if (res.success && res.data) {
                this.availableSkills = res.data;
                this.renderRegSkillChips();
            }
        } catch (err) {
            console.error('Failed to load skills catalog:', err);
        }
    }

    renderRegSkillChips() {
        const container = document.getElementById('reg-skill-chips');
        if (!container || !this.availableSkills || this.availableSkills.length === 0) return;

        container.innerHTML = this.availableSkills.map(s => {
            const isSelected = this.selectedRegSkills.has(s.name);
            return `<span class="skill-chip ${isSelected ? 'selected' : ''}" onclick="app.toggleRegSkill('${this.escapeHtml(s.name)}')">
                ${isSelected ? '<i class="fa-solid fa-check"></i>' : '<i class="fa-solid fa-plus"></i>'} ${this.escapeHtml(s.name)}
            </span>`;
        }).join('');
    }

    toggleRegSkill(name) {
        if (this.selectedRegSkills.has(name)) {
            this.selectedRegSkills.delete(name);
        } else {
            this.selectedRegSkills.add(name);
        }
        this.renderRegSkillChips();
        const input = document.getElementById('reg-skills');
        if (input) input.value = Array.from(this.selectedRegSkills).join(', ');
    }

    renderProjectSkillChips() {
        const container = document.getElementById('new-project-skill-chips');
        if (!container || !this.availableSkills || this.availableSkills.length === 0) return;

        container.innerHTML = this.availableSkills.map(s => {
            const isSelected = this.selectedProjectSkills.has(s.name);
            return `<span class="skill-chip ${isSelected ? 'selected' : ''}" onclick="app.toggleProjectSkill('${this.escapeHtml(s.name)}')">
                ${isSelected ? '<i class="fa-solid fa-check"></i>' : '<i class="fa-solid fa-plus"></i>'} ${this.escapeHtml(s.name)}
            </span>`;
        }).join('');
    }

    toggleProjectSkill(name) {
        if (this.selectedProjectSkills.has(name)) {
            this.selectedProjectSkills.delete(name);
        } else {
            this.selectedProjectSkills.add(name);
        }
        this.renderProjectSkillChips();
        const input = document.getElementById('new-project-skills');
        if (input) input.value = Array.from(this.selectedProjectSkills).join(', ');
    }

    // Protected Action Guards
    handlePostProjectClick() {
        if (!this.currentUser) {
            this.showToast('Please sign in as a Client to post a project, or register first!', 'warning');
            this.openLoginModal('CLIENT');
            return;
        }

        if (this.currentUser.role !== 'CLIENT') {
            this.showToast('You are signed in as a Freelancer. Only Clients can post project listings.', 'warning');
            return;
        }

        this.selectedProjectSkills.clear();
        this.renderProjectSkillChips();
        const form = document.getElementById('create-project-form');
        if (form) form.reset();
        this.showModal('create-project-modal');
    }

    logout() {
        this.currentUser = null;
        localStorage.removeItem('fh_user');
        this.updateAuthUI();
        this.closeAllDropdowns();
        this.showToast('You have been signed out successfully.', 'info');
        this.navigate('home');
        this.loadHomeStats();
    }

    // ========================================================
    // LANDING & HOME DATA
    // ========================================================
    async loadHomeStats() {
        try {
            const res = await this.fetchApi('/dashboard/stats', 'GET');
            if (res.success && res.data) {
                const flEl = document.getElementById('stat-freelancers');
                const prEl = document.getElementById('stat-projects');
                const cmEl = document.getElementById('stat-completed');
                const clEl = document.getElementById('stat-clients');

                if (flEl) flEl.textContent = (res.data.totalFreelancers !== undefined ? res.data.totalFreelancers : 0).toLocaleString();
                if (prEl) prEl.textContent = (res.data.totalProjects !== undefined ? res.data.totalProjects : 0).toLocaleString();
                if (cmEl) cmEl.textContent = (res.data.completedProjects !== undefined ? res.data.completedProjects : 0).toLocaleString();
                if (clEl) clEl.textContent = (res.data.totalClients !== undefined ? res.data.totalClients : 0).toLocaleString();
            }
        } catch (err) {
            console.error('Failed to load stats:', err);
        }
    }

    async loadFeaturedProjects() {
        try {
            const res = await this.fetchApi('/projects?status=OPEN', 'GET');
            const container = document.getElementById('home-featured-projects');
            if (!container) return;

            if (res.success && res.data && res.data.length > 0) {
                container.innerHTML = res.data.slice(0, 3).map(p => this.renderProjectCard(p)).join('');
            } else {
                container.innerHTML = `
                    <div style="grid-column: 1 / -1; text-align: center; padding: 3rem 1.5rem; background: var(--bg-card); border-radius: var(--radius-lg); border: 1px dashed var(--border-color);">
                        <i class="fa-solid fa-rocket" style="font-size: 2.5rem; color: var(--primary); margin-bottom: 0.85rem; opacity: 0.85;"></i>
                        <h4 style="font-size: 1.2rem; margin-bottom: 0.4rem;">No Active Projects Posted Yet</h4>
                        <p style="color: var(--text-secondary); font-size: 0.92rem; margin-bottom: 1.25rem;">Be the first client to publish an opportunity and discover skilled developers!</p>
                        <button class="btn btn-primary btn-sm" onclick="app.handlePostProjectClick()">
                            <i class="fa-solid fa-plus"></i> Post a Project
                        </button>
                    </div>
                `;
            }
        } catch (err) {
            console.error('Failed to load featured projects:', err);
        }
    }

    // ========================================================
    // PROJECTS MARKETPLACE (SEARCH & FILTERS)
    // ========================================================
    async loadSkillsFilter() {
        try {
            const res = await this.fetchApi('/skills', 'GET');
            if (res.success && res.data) {
                const projSkillSelect = document.getElementById('filter-skill');
                const flSkillSelect = document.getElementById('filter-freelancer-skill');

                const options = res.data.map(s => `<option value="${this.escapeHtml(s.name)}">${this.escapeHtml(s.name)}</option>`).join('');
                if (projSkillSelect) projSkillSelect.innerHTML = '<option value="">All Skills</option>' + options;
                if (flSkillSelect) flSkillSelect.innerHTML = '<option value="">Filter by Skill</option>' + options;
            }
        } catch (err) {
            console.error('Error loading skills filter:', err);
        }
    }

    debounceSearch() {
        clearTimeout(this.searchDebounceTimer);
        this.searchDebounceTimer = setTimeout(() => {
            this.loadProjects();
        }, 300);
    }

    async loadProjects() {
        const query = document.getElementById('search-query-input')?.value || '';
        const category = document.getElementById('filter-category')?.value || 'ALL';
        const skill = document.getElementById('filter-skill')?.value || '';
        const status = document.getElementById('filter-status')?.value || 'ALL';
        const sort = document.getElementById('filter-sort')?.value || 'newest';

        const params = new URLSearchParams();
        if (query.trim()) params.append('query', query.trim());
        if (category && category !== 'ALL') params.append('category', category);
        if (skill) params.append('skill', skill);
        if (status && status !== 'ALL') params.append('status', status);
        if (sort) params.append('sortBy', sort);

        const container = document.getElementById('projects-list-container');
        if (container) {
            container.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 3rem; color: var(--text-muted);"><i class="fa-solid fa-spinner fa-spin fa-2x"></i><p style="margin-top: 0.5rem;">Loading projects...</p></div>';
        }

        try {
            const res = await this.fetchApi('/projects?' + params.toString(), 'GET');
            if (res.success && res.data) {
                if (res.data.length === 0) {
                    container.innerHTML = `
                        <div class="empty-state-box">
                            <div class="empty-icon"><i class="fa-solid fa-folder-open"></i></div>
                            <h3>No Projects Available Yet</h3>
                            <p>There are currently no active projects in the marketplace. Are you a client looking to launch an initiative?</p>
                            <button class="btn btn-primary" onclick="app.handlePostProjectClick()">
                                <i class="fa-solid fa-plus"></i> Post the First Project
                            </button>
                        </div>
                    `;
                } else {
                    container.innerHTML = res.data.map(p => this.renderProjectCard(p)).join('');
                }
            }
        } catch (err) {
            if (container) {
                container.innerHTML = `<div style="grid-column: 1/-1; color: var(--danger); text-align: center; padding: 2rem;">Failed to load projects: ${err.message}</div>`;
            }
        }
    }

    renderProjectCard(p) {
        const skillsHtml = (p.requiredSkills || []).map(s => `<span class="skill-pill">${this.escapeHtml(s.name)}</span>`).join('');
        const statusClass = 'status-' + (p.status || 'open').toLowerCase().replace('_', '-');
        const formattedMin = Number(p.budgetMin || 0).toLocaleString();
        const formattedMax = Number(p.budgetMax || 0).toLocaleString();

        return `
            <div class="project-card">
                <div>
                    <div class="project-meta-top">
                        <span class="project-category">${this.escapeHtml(p.category || 'General')}</span>
                        <span class="status-badge ${statusClass}">${this.escapeHtml(p.status || 'OPEN')}</span>
                    </div>
                    <h3 class="project-title">${this.escapeHtml(p.title)}</h3>
                    <div class="project-client-sub">
                        <i class="fa-solid fa-building"></i> ${this.escapeHtml(p.clientCompanyName || 'Enterprise Client')}
                        ${p.clientLocation ? `<span>• <i class="fa-solid fa-location-dot"></i> ${this.escapeHtml(p.clientLocation)}</span>` : ''}
                    </div>
                    <p class="project-desc">${this.escapeHtml(p.description)}</p>
                    <div class="skills-wrapper">
                        ${skillsHtml}
                    </div>
                </div>

                <div class="project-card-footer">
                    <div>
                        <div class="budget-sub">Budget</div>
                        <div class="budget-tag">₹${formattedMin} - ₹${formattedMax}</div>
                    </div>
                    <button class="btn btn-outline btn-sm" onclick="app.navigate('project-details', new URLSearchParams({ id: ${p.id} }))">
                        View Details <i class="fa-solid fa-arrow-right"></i>
                    </button>
                </div>
            </div>
        `;
    }

    // ========================================================
    // PROJECT DETAILS, SKILL MATCH & TIMELINE
    // ========================================================
    async loadProjectDetails(projectId) {
        this.currentProjectId = projectId;
        const detailsContainer = document.getElementById('project-details-card');
        const timelineContainer = document.getElementById('project-timeline-stepper');
        const collabContainer = document.getElementById('project-collab-section');

        detailsContainer.innerHTML = '<div style="text-align: center; padding: 3rem;"><i class="fa-solid fa-spinner fa-spin fa-2x"></i></div>';

        try {
            const res = await this.fetchApi('/projects/' + projectId, 'GET');
            if (!res.success || !res.data) {
                detailsContainer.innerHTML = '<div style="color: var(--danger); text-align: center;">Project not found.</div>';
                return;
            }

            const p = res.data;
            const statusClass = 'status-' + (p.status || 'open').toLowerCase().replace('_', '-');

            // Check if current user is the owner client or assigned freelancer
            const isClientOwner = this.currentUser && this.currentUser.role === 'CLIENT' && this.currentUser.profileId === p.clientId;
            const isAssignedFreelancer = this.currentUser && this.currentUser.role === 'FREELANCER' && this.currentUser.profileId === p.assignedFreelancerId;

            // Load Skill Match if Freelancer is viewing
            let skillMatchHtml = '';
            if (this.currentUser && this.currentUser.role === 'FREELANCER') {
                try {
                    const matchRes = await this.fetchApi(`/projects/${p.id}/match/${this.currentUser.profileId}`, 'GET');
                    if (matchRes.success && matchRes.data) {
                        const m = matchRes.data;
                        const matchColor = m.matchPercentage >= 75 ? 'var(--success)' : (m.matchPercentage >= 50 ? 'var(--warning)' : 'var(--danger)');
                        const matchedSkillsPills = (m.matchedSkills || []).map(s => `<span class="skill-pill matched"><i class="fa-solid fa-check"></i> ${this.escapeHtml(s)}</span>`).join('');
                        const missingSkillsPills = (m.missingSkills || []).map(s => `<span class="skill-pill missing"><i class="fa-solid fa-xmark"></i> ${this.escapeHtml(s)}</span>`).join('');

                        skillMatchHtml = `
                            <div class="skill-match-card">
                                <div class="skill-match-header">
                                    <div>
                                        <h4 style="font-size: 1rem; margin-bottom: 0.2rem;"><i class="fa-solid fa-bullseye"></i> Skill Match Analysis</h4>
                                        <p style="font-size: 0.8rem; color: var(--text-secondary);">Calculated using your profile skills against project requirements</p>
                                    </div>
                                    <div class="match-circle-gauge" style="color: ${matchColor};">${m.matchPercentage}% Match</div>
                                </div>
                                <div style="display: flex; flex-wrap: wrap; gap: 0.5rem; margin-top: 0.75rem;">
                                    ${matchedSkillsPills}
                                    ${missingSkillsPills}
                                </div>
                            </div>
                        `;
                    }
                } catch (e) {
                    console.error('Match check error', e);
                }
            }

            // Action button logic
            let actionBtnHtml = '';
            if (this.currentUser && this.currentUser.role === 'FREELANCER') {
                if (p.status === 'OPEN') {
                    actionBtnHtml = `
                        <button class="btn btn-primary btn-lg" onclick="app.openApplyModal(${p.id}, '${this.escapeHtml(p.title)}')">
                            <i class="fa-solid fa-paper-plane"></i> Apply for this Project
                        </button>
                    `;
                } else if (isAssignedFreelancer) {
                    actionBtnHtml = `
                        <button class="btn btn-success btn-lg" onclick="app.openChatModal(${p.id}, '${this.escapeHtml(p.title)}')">
                            <i class="fa-solid fa-comments"></i> Chat with Client
                        </button>
                    `;
                } else {
                    actionBtnHtml = `<span class="skill-pill" style="padding: 0.5rem 1rem; font-size: 0.85rem;">Project is ${p.status}</span>`;
                }
            } else if (isClientOwner) {
                actionBtnHtml = `
                    <div style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
                        <button class="btn btn-outline" onclick="app.openApplicantsModal(${p.id}, '${this.escapeHtml(p.title)}')">
                            <i class="fa-solid fa-users"></i> View Applicants (${p.applicantCount || 0})
                        </button>
                        <button class="btn btn-secondary" onclick="app.openStatusModal(${p.id}, '${p.status}')">
                            <i class="fa-solid fa-pen-to-square"></i> Update Status
                        </button>
                        ${p.assignedFreelancerId ? `
                            <button class="btn btn-primary" onclick="app.openChatModal(${p.id}, '${this.escapeHtml(p.title)}')">
                                <i class="fa-solid fa-comments"></i> Message Freelancer
                            </button>
                        ` : ''}
                    </div>
                `;
            } else if (!this.currentUser) {
                actionBtnHtml = `
                    <button class="btn btn-primary btn-lg" onclick="app.openLoginModal('FREELANCER')">
                        <i class="fa-solid fa-lock"></i> Sign In as Freelancer to Apply
                    </button>
                `;
            }

            const skillsHtml = (p.requiredSkills || []).map(s => `<span class="skill-pill" style="font-size: 0.82rem; padding: 0.35rem 0.85rem;">${this.escapeHtml(s.name)}</span>`).join('');

            detailsContainer.innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem; margin-bottom: 1.5rem;">
                    <div>
                        <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem;">
                            <span class="project-category">${this.escapeHtml(p.category)}</span>
                            <span class="status-badge ${statusClass}">${this.escapeHtml(p.status)}</span>
                        </div>
                        <h1 style="font-size: 2rem; margin-bottom: 0.5rem;">${this.escapeHtml(p.title)}</h1>
                        <p style="color: var(--text-secondary); display: flex; align-items: center; gap: 0.5rem;">
                            <i class="fa-solid fa-building"></i> <strong>${this.escapeHtml(p.clientCompanyName || 'Client')}</strong>
                            <span>•</span>
                            <i class="fa-solid fa-calendar"></i> Deadline: <strong>${p.deadline}</strong>
                            <span>•</span>
                            <i class="fa-solid fa-users"></i> <strong>${p.applicantCount || 0} applicants</strong>
                        </p>
                    </div>

                    <div style="text-align: right;">
                        <div class="budget-sub">Project Budget</div>
                        <div class="budget-tag" style="font-size: 1.85rem; color: var(--primary);">₹${Number(p.budgetMin).toLocaleString()} - ₹${Number(p.budgetMax).toLocaleString()}</div>
                    </div>
                </div>

                ${skillMatchHtml}

                <div style="margin: 2rem 0;">
                    <h3 style="font-size: 1.15rem; margin-bottom: 0.75rem;">Required Technical Skills</h3>
                    <div class="skills-wrapper">
                        ${skillsHtml}
                    </div>
                </div>

                <div style="margin: 2rem 0;">
                    <h3 style="font-size: 1.15rem; margin-bottom: 0.75rem;">Project Description & Specifications</h3>
                    <div style="font-size: 0.95rem; line-height: 1.7; color: var(--text-secondary); white-space: pre-line; background: var(--bg-card-subtle); padding: 1.5rem; border-radius: var(--radius-md); border: 1px solid var(--border-color);">
                        ${this.escapeHtml(p.description)}
                    </div>
                </div>

                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 2rem; padding-top: 1.5rem; border-top: 1px solid var(--border-color); flex-wrap: wrap; gap: 1rem;">
                    <div>
                        ${p.assignedFreelancerName ? `
                            <div style="display: flex; align-items: center; gap: 0.5rem; font-size: 0.9rem;">
                                <i class="fa-solid fa-circle-check" style="color: var(--success);"></i>
                                Assigned to: <strong>${this.escapeHtml(p.assignedFreelancerName)}</strong>
                            </div>
                        ` : ''}
                    </div>
                    <div>
                        ${actionBtnHtml}
                    </div>
                </div>
            `;

            // Render Timeline Stepper (Section 23)
            this.renderTimeline(p.status, timelineContainer);

            // Render Collaboration / Chat / Reviews Section
            this.renderCollabSection(p, collabContainer);

        } catch (err) {
            detailsContainer.innerHTML = `<div style="color: var(--danger); text-align: center;">Error loading project details: ${err.message}</div>`;
        }
    }

    renderTimeline(currentStatus, container) {
        const steps = [
            { key: 'OPEN', label: 'Open' },
            { key: 'ASSIGNED', label: 'Assigned' },
            { key: 'IN_PROGRESS', label: 'In Progress' },
            { key: 'UNDER_REVIEW', label: 'Under Review' },
            { key: 'COMPLETED', label: 'Completed' }
        ];

        const stepKeys = steps.map(s => s.key);
        const currentIndex = stepKeys.indexOf(currentStatus.toUpperCase());

        container.innerHTML = steps.map((step, idx) => {
            let stateClass = '';
            let icon = idx + 1;

            if (currentStatus === 'CANCELLED') {
                stateClass = 'cancelled';
                icon = '<i class="fa-solid fa-ban"></i>';
            } else if (idx < currentIndex) {
                stateClass = 'completed';
                icon = '<i class="fa-solid fa-check"></i>';
            } else if (idx === currentIndex) {
                stateClass = 'active';
                icon = '<i class="fa-solid fa-circle"></i>';
            }

            return `
                <div class="step-item ${stateClass}">
                    <div class="step-circle">${icon}</div>
                    <div class="step-label">${step.label}</div>
                </div>
            `;
        }).join('');
    }

    async renderCollabSection(p, container) {
        if (!container) return;

        // If completed, show reviews
        if (p.status === 'COMPLETED') {
            try {
                const revRes = await this.fetchApi(`/reviews/project/${p.id}`, 'GET');
                const reviews = revRes.success ? (revRes.data || []) : [];

                let revHtml = `
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
                        <h3 style="font-size: 1.15rem;"><i class="fa-solid fa-star" style="color: #fbbf24;"></i> Project Reviews & Ratings</h3>
                        ${(this.currentUser && (this.currentUser.role === 'CLIENT' || this.currentUser.role === 'FREELANCER')) ? `
                            <button class="btn btn-primary btn-sm" onclick="app.openReviewModal(${p.id})">
                                <i class="fa-solid fa-pen"></i> Leave a Review
                            </button>
                        ` : ''}
                    </div>
                `;

                if (reviews.length === 0) {
                    revHtml += `<p style="color: var(--text-muted); font-size: 0.9rem;">No reviews submitted yet for this completed project.</p>`;
                } else {
                    revHtml += reviews.map(r => `
                        <div style="background: var(--bg-card-subtle); padding: 1.25rem; border-radius: var(--radius-md); border: 1px solid var(--border-color); margin-bottom: 1rem;">
                            <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem;">
                                <strong>${this.escapeHtml(r.reviewerName)} <span style="font-size: 0.75rem; color: var(--text-muted); font-weight: normal;">(${r.reviewerRole})</span></strong>
                                <div style="color: #fbbf24;">${'★'.repeat(r.rating)}${'☆'.repeat(5 - r.rating)}</div>
                            </div>
                            <p style="font-size: 0.88rem; color: var(--text-secondary);">${this.escapeHtml(r.comment)}</p>
                        </div>
                    `).join('');
                }

                container.innerHTML = revHtml;
                container.style.display = 'block';
            } catch (e) {
                console.error('Error loading reviews:', e);
            }
        } else {
            container.style.display = 'none';
        }
    }

    // ========================================================
    // APPLICATION ACTIONS (APPLY, ACCEPT, REJECT)
    // ========================================================
    openApplyModal(projectId, projectTitle) {
        if (!this.currentUser) {
            this.showToast('Please sign in as a Freelancer to submit a proposal, or register first!', 'warning');
            this.openLoginModal('FREELANCER');
            return;
        }

        if (this.currentUser.role !== 'FREELANCER') {
            this.showToast('You are signed in as a Client. Only Freelancers can submit proposals.', 'warning');
            return;
        }

        document.getElementById('apply-project-id').value = projectId;
        document.getElementById('apply-modal-project-title').textContent = projectTitle;
        document.getElementById('apply-proposal').value = '';
        document.getElementById('apply-budget').value = '';
        document.getElementById('apply-days').value = '';
        this.showModal('apply-modal');
    }

    async handleApply(e) {
        e.preventDefault();
        const projectId = document.getElementById('apply-project-id').value;
        const proposal = document.getElementById('apply-proposal').value;
        const proposedBudget = parseFloat(document.getElementById('apply-budget').value);
        const estimatedDays = parseInt(document.getElementById('apply-days').value, 10);

        const payload = {
            freelancerId: this.currentUser.profileId,
            proposal,
            proposedBudget,
            estimatedDays
        };

        try {
            const res = await this.fetchApi(`/projects/${projectId}/apply`, 'POST', payload);
            if (res.success) {
                this.closeModal('apply-modal');
                this.showToast('Application submitted successfully! 🎉', 'success');
                this.loadProjectDetails(projectId);
            } else {
                this.showToast(res.message || 'Application failed', 'error');
            }
        } catch (err) {
            this.showToast(err.message || 'Error submitting application', 'error');
        }
    }

    async openApplicantsModal(projectId, projectTitle) {
        document.getElementById('applicants-modal-project-title').textContent = projectTitle;
        const body = document.getElementById('applicants-modal-body');
        body.innerHTML = '<div style="text-align: center; padding: 2rem;"><i class="fa-solid fa-spinner fa-spin fa-2x"></i></div>';
        this.showModal('applicants-modal');

        try {
            const res = await this.fetchApi(`/projects/${projectId}/applications`, 'GET');
            if (res.success && res.data) {
                if (res.data.length === 0) {
                    body.innerHTML = `<div style="text-align: center; padding: 3rem; color: var(--text-muted);">No candidates have applied to this project yet.</div>`;
                    return;
                }

                body.innerHTML = res.data.map(app => {
                    const statusBadge = `<span class="status-badge status-${(app.status || 'pending').toLowerCase()}">${app.status}</span>`;
                    const matchPercent = app.skillMatchPercentage !== undefined ? app.skillMatchPercentage : 80;
                    const skillsPills = (app.freelancerSkills || []).map(s => `<span class="skill-pill">${this.escapeHtml(s.name)}</span>`).join('');
                    const profileImage = app.freelancerProfileImage
                        ? `<img src="${this.escapeHtml(app.freelancerProfileImage)}" alt="" style="width: 44px; height: 44px; border-radius: 50%; object-fit: cover;">`
                        : '';

                    return `
                        <div style="background: var(--bg-card-subtle); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 1.25rem; margin-bottom: 1rem;">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 0.75rem; margin-bottom: 0.75rem;">
                                <div style="display: flex; gap: 0.75rem; align-items: center;">
                                    ${profileImage}
                                    <div>
                                        <div style="font-weight: 700; font-size: 1rem;">${this.escapeHtml(app.freelancerName)}</div>
                                        <div style="font-size: 0.78rem; color: var(--text-muted);">${this.escapeHtml(app.freelancerHeadline || 'Professional Freelancer')}</div>
                                    </div>
                                </div>
                                <div style="text-align: right;">
                                    <div style="font-weight: 800; font-size: 1.15rem; color: var(--primary);">₹${Number(app.proposedBudget).toLocaleString()}</div>
                                    <div style="font-size: 0.72rem; color: var(--text-muted);">Delivery in ${app.estimatedDays} days</div>
                                </div>
                            </div>

                            <div style="display: flex; align-items: center; gap: 1rem; margin-bottom: 0.75rem; font-size: 0.8rem;">
                                <span style="color: #fbbf24;"><i class="fa-solid fa-star"></i> ${app.freelancerRating || 4.9}</span>
                                <span style="color: var(--success); font-weight: 700;"><i class="fa-solid fa-bullseye"></i> ${matchPercent}% Skill Match</span>
                                ${statusBadge}
                            </div>

                            <p style="font-size: 0.88rem; color: var(--text-secondary); margin-bottom: 0.75rem; line-height: 1.5; background: var(--bg-card); padding: 0.75rem; border-radius: var(--radius-sm);">
                                "${this.escapeHtml(app.proposal)}"
                            </p>

                            <div class="skills-wrapper" style="margin-bottom: 1rem;">
                                ${skillsPills}
                            </div>

                            ${app.status === 'PENDING' ? `
                                <div style="display: flex; justify-content: flex-end; gap: 0.5rem;">
                                    <button class="btn btn-outline btn-sm" onclick="app.rejectApplication(${app.id}, ${projectId})">
                                        <i class="fa-solid fa-xmark"></i> Reject
                                    </button>
                                    <button class="btn btn-success btn-sm" onclick="app.acceptApplication(${app.id}, ${projectId})">
                                        <i class="fa-solid fa-check"></i> Accept & Assign
                                    </button>
                                </div>
                            ` : ''}
                        </div>
                    `;
                }).join('');
            }
        } catch (err) {
            body.innerHTML = `<div style="color: var(--danger);">Failed to load applicants: ${err.message}</div>`;
        }
    }

    async acceptApplication(applicationId, projectId) {
        if (!confirm('Are you sure you want to accept this candidate and assign the project?')) return;

        try {
            const res = await this.fetchApi(`/applications/${applicationId}/accept?clientId=${this.currentUser.profileId}`, 'PUT');
            if (res.success) {
                this.showToast('Candidate accepted! Project assigned successfully.', 'success');
                this.closeModal('applicants-modal');
                this.loadProjectDetails(projectId);
                this.loadNotificationCount();
            } else {
                this.showToast(res.message || 'Acceptance failed', 'error');
            }
        } catch (err) {
            this.showToast(err.message || 'Error accepting candidate', 'error');
        }
    }

    async rejectApplication(applicationId, projectId) {
        if (!confirm('Are you sure you want to reject this proposal?')) return;

        try {
            const res = await this.fetchApi(`/applications/${applicationId}/reject?clientId=${this.currentUser.profileId}`, 'PUT');
            if (res.success) {
                this.showToast('Application rejected.', 'info');
                this.openApplicantsModal(projectId, document.getElementById('applicants-modal-project-title').textContent);
            } else {
                this.showToast(res.message || 'Rejection failed', 'error');
            }
        } catch (err) {
            this.showToast(err.message || 'Error rejecting candidate', 'error');
        }
    }

    // ========================================================
    // STATUS TRANSITION WORKFLOW (Section 23)
    // ========================================================
    openStatusModal(projectId, currentStatus) {
        document.getElementById('status-project-id').value = projectId;
        document.getElementById('status-current-display').value = currentStatus;

        const select = document.getElementById('status-next-select');
        select.innerHTML = '';

        // Enforce valid transitions from Section 23
        const transitions = {
            'OPEN': ['ASSIGNED', 'CANCELLED'],
            'ASSIGNED': ['IN_PROGRESS', 'CANCELLED'],
            'IN_PROGRESS': ['UNDER_REVIEW', 'CANCELLED'],
            'UNDER_REVIEW': ['COMPLETED', 'IN_PROGRESS', 'CANCELLED']
        };

        const available = transitions[currentStatus.toUpperCase()] || [];
        if (available.length === 0) {
            this.showToast('No further status transitions available for ' + currentStatus, 'info');
            return;
        }

        available.forEach(st => {
            const opt = document.createElement('option');
            opt.value = st;
            opt.textContent = st.replace('_', ' ');
            select.appendChild(opt);
        });

        this.showModal('status-modal');
    }

    async handleUpdateStatus(e) {
        e.preventDefault();
        const projectId = document.getElementById('status-project-id').value;
        const nextStatus = document.getElementById('status-next-select').value;

        try {
            const res = await this.fetchApi(`/projects/${projectId}/status`, 'PUT', {
                status: nextStatus,
                changedBy: this.currentUser.userId
            });

            if (res.success) {
                this.closeModal('status-modal');
                this.showToast('Project status updated to ' + nextStatus + '!', 'success');
                this.loadProjectDetails(projectId);
                this.loadNotificationCount();
            } else {
                this.showToast(res.message || 'Status transition failed', 'error');
            }
        } catch (err) {
            this.showToast(err.message || 'Error updating status', 'error');
        }
    }

    // ========================================================
    // FREELANCER DASHBOARD (Section 15)
    // ========================================================
    async loadFreelancerDashboard() {
        if (!this.currentUser || this.currentUser.role !== 'FREELANCER') return;

        try {
            const res = await this.fetchApi(`/dashboard/freelancer/${this.currentUser.profileId}`, 'GET');
            if (res.success && res.data) {
                const d = res.data;
                document.getElementById('fl-dash-welcome').textContent = `Welcome back, ${this.currentUser.name} 👋`;
                document.getElementById('fl-dash-name').textContent = this.currentUser.name;
                const avatar = document.getElementById('fl-dash-avatar');
                if (this.currentUser.profileImage) {
                    avatar.src = this.currentUser.profileImage;
                    avatar.style.display = '';
                } else {
                    avatar.removeAttribute('src');
                    avatar.style.display = 'none';
                }

                document.getElementById('fl-kpi-applied').textContent = d.appliedProjects || 0;
                document.getElementById('fl-kpi-active').textContent = d.activeProjects || 0;
                document.getElementById('fl-kpi-completed').textContent = d.completedProjects || 0;
                document.getElementById('fl-kpi-earnings').textContent = '₹' + Number(d.totalEarnings || 0).toLocaleString();

                // Active projects with progress bars (Section 15)
                const activeContainer = document.getElementById('fl-active-projects-list');
                const activeList = d.myActiveProjects || [];

                if (activeList.length === 0) {
                    activeContainer.innerHTML = '<p style="color: var(--text-muted); font-size: 0.9rem;">You have no active project contracts currently.</p>';
                } else {
                    activeContainer.innerHTML = activeList.map(item => `
                        <div style="background: var(--bg-card-subtle); padding: 1.25rem; border-radius: var(--radius-md); border: 1px solid var(--border-color); margin-bottom: 1rem;">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                                <strong>${this.escapeHtml(item.title)}</strong>
                                <span class="status-badge status-${(item.status || '').toLowerCase().replace('_', '-')}">${item.status}</span>
                            </div>
                            <div style="display: flex; justify-content: space-between; font-size: 0.78rem; color: var(--text-muted); margin-bottom: 0.25rem;">
                                <span>Progress</span>
                                <strong>${item.progressPercent}%</strong>
                            </div>
                            <div class="progress-container">
                                <div class="progress-bar" style="width: ${item.progressPercent}%;"></div>
                            </div>
                            <div style="display: flex; justify-content: flex-end; margin-top: 0.75rem;">
                                <button class="btn btn-outline btn-sm" onclick="app.navigate('project-details', new URLSearchParams({ id: ${item.id} }))">
                                    Open Workspace <i class="fa-solid fa-arrow-right"></i>
                                </button>
                            </div>
                        </div>
                    `).join('');
                }

                // Applications history
                const tbody = document.getElementById('fl-applications-tbody');
                const apps = d.recentApplications || [];
                if (apps.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted);">No applications submitted yet.</td></tr>';
                } else {
                    tbody.innerHTML = apps.map(a => `
                        <tr>
                            <td><strong>${this.escapeHtml(a.projectTitle || 'Project #' + a.projectId)}</strong></td>
                            <td>₹${Number(a.proposedBudget).toLocaleString()}</td>
                            <td>${a.estimatedDays} Days</td>
                            <td><span style="color: var(--success); font-weight: 700;">${a.skillMatchPercentage !== undefined ? a.skillMatchPercentage + '%' : '85%'}</span></td>
                            <td><span class="status-badge status-${(a.status || 'pending').toLowerCase()}">${a.status}</span></td>
                            <td style="color: var(--text-muted); font-size: 0.8rem;">${(a.appliedAt || '').split('T')[0]}</td>
                        </tr>
                    `).join('');
                }
            }
        } catch (err) {
            console.error('Error loading freelancer dashboard:', err);
        }
    }

    // ========================================================
    // CLIENT DASHBOARD (Section 16)
    // ========================================================
    async loadClientDashboard() {
        if (!this.currentUser || this.currentUser.role !== 'CLIENT') return;

        try {
            const res = await this.fetchApi(`/dashboard/client/${this.currentUser.profileId}`, 'GET');
            if (res.success && res.data) {
                const d = res.data;
                document.getElementById('client-dash-welcome').textContent = `Welcome back, ${this.currentUser.name} 🏢`;
                document.getElementById('client-dash-company').textContent = this.currentUser.name;
                const avatar = document.getElementById('client-dash-avatar');
                if (this.currentUser.profileImage) {
                    avatar.src = this.currentUser.profileImage;
                    avatar.style.display = '';
                } else {
                    avatar.removeAttribute('src');
                    avatar.style.display = 'none';
                }

                document.getElementById('client-kpi-total').textContent = d.totalProjects || 0;
                document.getElementById('client-kpi-open').textContent = d.openProjects || 0;
                document.getElementById('client-kpi-inprogress').textContent = d.inProgressProjects || 0;
                document.getElementById('client-kpi-spent').textContent = '₹' + Number(d.totalSpent || 0).toLocaleString();

                // Projects Table
                const tbody = document.getElementById('client-projects-tbody');
                const projs = d.projects || [];
                if (projs.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted);">You have not created any projects yet. Click "Post New Project" above.</td></tr>';
                } else {
                    tbody.innerHTML = projs.map(p => `
                        <tr>
                            <td><strong>${this.escapeHtml(p.title)}</strong></td>
                            <td>₹${Number(p.budgetMin).toLocaleString()} - ₹${Number(p.budgetMax).toLocaleString()}</td>
                            <td>${p.deadline}</td>
                            <td><span class="skill-pill">${p.applicantCount || 0} Applicants</span></td>
                            <td><span class="status-badge status-${(p.status || 'open').toLowerCase().replace('_', '-')}">${p.status}</span></td>
                            <td>
                                <div style="display: flex; gap: 0.4rem;">
                                    <button class="btn btn-outline btn-sm" onclick="app.navigate('project-details', new URLSearchParams({ id: ${p.id} }))">
                                        View
                                    </button>
                                    <button class="btn btn-secondary btn-sm" onclick="app.openApplicantsModal(${p.id}, '${this.escapeHtml(p.title)}')">
                                        Applicants
                                    </button>
                                </div>
                            </td>
                        </tr>
                    `).join('');
                }

                // Recent applicants stream
                const stream = document.getElementById('client-recent-applicants-stream');
                const apps = d.recentApplicants || [];
                if (apps.length === 0) {
                    stream.innerHTML = '<p style="color: var(--text-muted); font-size: 0.9rem;">No applications received recently.</p>';
                } else {
                    stream.innerHTML = apps.slice(0, 5).map(a => `
                        <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.85rem; border-bottom: 1px solid var(--border-color); flex-wrap: wrap; gap: 0.5rem;">
                            <div>
                                <strong>${this.escapeHtml(a.freelancerName)}</strong> applied to <em>"${this.escapeHtml(a.projectTitle)}"</em>
                                <div style="font-size: 0.78rem; color: var(--text-muted);">Proposed: ₹${Number(a.proposedBudget).toLocaleString()} in ${a.estimatedDays} days</div>
                            </div>
                            <span class="status-badge status-${(a.status || 'pending').toLowerCase()}">${a.status}</span>
                        </div>
                    `).join('');
                }
            }
        } catch (err) {
            console.error('Error loading client dashboard:', err);
        }
    }

    async handleCreateProject(e) {
        e.preventDefault();
        const title = document.getElementById('new-project-title').value;
        const category = document.getElementById('new-project-category').value;
        const deadline = document.getElementById('new-project-deadline').value;
        const budgetMin = parseFloat(document.getElementById('new-project-budget-min').value);
        const budgetMax = parseFloat(document.getElementById('new-project-budget-max').value);
        const skillsStr = document.getElementById('new-project-skills').value;
        const description = document.getElementById('new-project-description').value;

        const skillNames = skillsStr ? skillsStr.split(',').map(s => s.trim()).filter(Boolean) : [];

        const payload = {
            clientId: this.currentUser.profileId,
            title,
            category,
            deadline,
            budgetMin,
            budgetMax,
            description,
            skillNames
        };

        try {
            const res = await this.fetchApi('/projects', 'POST', payload);
            if (res.success) {
                this.closeModal('create-project-modal');
                this.showToast('Project listing published successfully! 🚀', 'success');
                this.loadClientDashboard();
                this.loadProjects();
            } else {
                this.showToast(res.message || 'Failed to create project', 'error');
            }
        } catch (err) {
            this.showToast(err.message || 'Error creating project', 'error');
        }
    }

    // ========================================================
    // ADMIN DASHBOARD & CHARTS (Section 17)
    // ========================================================
    async loadAdminDashboard() {
        if (!this.currentUser || this.currentUser.role !== 'ADMIN') return;

        try {
            const res = await this.fetchApi('/dashboard/admin', 'GET');
            if (res.success && res.data) {
                const d = res.data;
                document.getElementById('admin-kpi-users').textContent = d.totalUsers || 0;
                document.getElementById('admin-kpi-freelancers').textContent = d.totalFreelancers || 0;
                document.getElementById('admin-kpi-clients').textContent = d.totalClients || 0;
                document.getElementById('admin-kpi-projects').textContent = d.totalProjects || 0;

                // Render Chart.js
                this.renderAdminCharts(d);

                // Load User Moderation list
                this.loadAdminUsers();
            }
        } catch (err) {
            console.error('Error loading admin dashboard:', err);
        }
    }

    renderAdminCharts(d) {
        // Destroy existing instances if any
        if (this.charts.statusChart) this.charts.statusChart.destroy();
        if (this.charts.roleChart) this.charts.roleChart.destroy();
        if (this.charts.catChart) this.charts.catChart.destroy();

        const isDark = this.currentTheme === 'dark';
        const textColor = isDark ? '#94a3b8' : '#475569';

        // 1. Projects by Status (Doughnut)
        const statusMap = d.projectsByStatus || {};
        const statusLabels = Object.keys(statusMap);
        const statusData = Object.values(statusMap);

        const ctxStatus = document.getElementById('chartProjectsByStatus');
        if (ctxStatus) {
            this.charts.statusChart = new Chart(ctxStatus, {
                type: 'doughnut',
                data: {
                    labels: statusLabels,
                    datasets: [{
                        data: statusData,
                        backgroundColor: ['#10b981', '#3b82f6', '#6366f1', '#f59e0b', '#06b6d4', '#ef4444'],
                        borderWidth: 0
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'bottom', labels: { color: textColor } }
                    }
                }
            });
        }

        // 2. Users by Role (Pie)
        const roleMap = d.usersByRole || {};
        const roleLabels = Object.keys(roleMap);
        const roleData = Object.values(roleMap);

        const ctxRole = document.getElementById('chartUsersByRole');
        if (ctxRole) {
            this.charts.roleChart = new Chart(ctxRole, {
                type: 'pie',
                data: {
                    labels: roleLabels,
                    datasets: [{
                        data: roleData,
                        backgroundColor: ['#6366f1', '#10b981', '#f59e0b'],
                        borderWidth: 0
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'bottom', labels: { color: textColor } }
                    }
                }
            });
        }

        // 3. Projects by Category (Bar)
        const catMap = d.projectsByCategory || {};
        const catLabels = Object.keys(catMap);
        const catData = Object.values(catMap);

        const ctxCat = document.getElementById('chartProjectsByCategory');
        if (ctxCat) {
            this.charts.catChart = new Chart(ctxCat, {
                type: 'bar',
                data: {
                    labels: catLabels,
                    datasets: [{
                        label: 'Projects',
                        data: catData,
                        backgroundColor: '#6366f1',
                        borderRadius: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                    scales: {
                        x: { ticks: { color: textColor } },
                        y: { ticks: { color: textColor, stepSize: 1 }, beginAtZero: true }
                    }
                }
            });
        }
    }

    async loadAdminUsers() {
        try {
            const res = await this.fetchApi('/admin/users', 'GET');
            const tbody = document.getElementById('admin-users-tbody');
            if (!tbody) return;

            if (res.success && res.data) {
                document.getElementById('admin-user-count').textContent = res.data.length + ' Registered Users';
                tbody.innerHTML = res.data.map(u => {
                    const isBlocked = u.status === 'BLOCKED';
                    const toggleAction = isBlocked ? 'ACTIVE' : 'BLOCKED';
                    const btnClass = isBlocked ? 'btn-success' : 'btn-danger';
                    const btnText = isBlocked ? 'Unblock' : 'Block';

                    return `
                        <tr>
                            <td>#${u.id}</td>
                            <td><strong>${this.escapeHtml(u.name)}</strong></td>
                            <td>${this.escapeHtml(u.email)}</td>
                            <td><span class="user-role-badge badge-${u.role.toLowerCase()}">${u.role}</span></td>
                            <td><span class="status-badge ${isBlocked ? 'status-cancelled' : 'status-open'}">${u.status}</span></td>
                            <td style="color: var(--text-muted); font-size: 0.8rem;">${(u.createdAt || '').split('T')[0]}</td>
                            <td>
                                ${u.role !== 'ADMIN' ? `
                                    <button class="btn ${btnClass} btn-sm" onclick="app.toggleUserBlock(${u.id}, '${toggleAction}')">
                                        ${btnText}
                                    </button>
                                ` : '<span style="color: var(--text-muted); font-size: 0.75rem;">Protected</span>'}
                            </td>
                        </tr>
                    `;
                }).join('');
            }
        } catch (err) {
            console.error('Error loading admin users:', err);
        }
    }

    async toggleUserBlock(userId, newStatus) {
        if (!confirm(`Are you sure you want to change user status to ${newStatus}?`)) return;

        try {
            const res = await this.fetchApi(`/admin/users/${userId}/status`, 'PUT', { status: newStatus });
            if (res.success) {
                this.showToast(`User status updated to ${newStatus}.`, 'success');
                this.loadAdminUsers();
            } else {
                this.showToast(res.message || 'Action failed', 'error');
            }
        } catch (err) {
            this.showToast(err.message || 'Error updating user status', 'error');
        }
    }

    // ========================================================
    // FREELANCERS DIRECTORY
    // ========================================================
    debounceFreelancers() {
        clearTimeout(this.searchDebounceTimer);
        this.searchDebounceTimer = setTimeout(() => {
            this.loadFreelancers();
        }, 300);
    }

    async loadFreelancers() {
        const query = document.getElementById('search-freelancer-input')?.value || '';
        const skill = document.getElementById('filter-freelancer-skill')?.value || '';

        const params = new URLSearchParams();
        if (query.trim()) params.append('query', query.trim());
        if (skill) params.append('skill', skill);

        const container = document.getElementById('freelancers-list-container');
        if (container) {
            container.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 3rem;"><i class="fa-solid fa-spinner fa-spin fa-2x"></i></div>';
        }

        try {
            const res = await this.fetchApi('/freelancers?' + params.toString(), 'GET');
            if (res.success && res.data) {
                if (res.data.length === 0) {
                    container.innerHTML = `
                        <div class="empty-state-box">
                            <div class="empty-icon"><i class="fa-solid fa-user-astronaut"></i></div>
                            <h3>No Freelancers Registered Yet</h3>
                            <p>Be the first expert software engineer, designer, or specialist to showcase your profile and start bidding on projects.</p>
                            <button class="btn btn-primary" onclick="app.openRegisterModal('FREELANCER')">
                                <i class="fa-solid fa-code"></i> Join as Freelancer
                            </button>
                        </div>
                    `;
                } else {
                    container.innerHTML = res.data.map(f => {
                        const skillsHtml = (f.skills || []).map(s => `<span class="skill-pill">${this.escapeHtml(s.name)}</span>`).join('');
                        const profileImage = f.profileImage
                            ? `<img src="${this.escapeHtml(f.profileImage)}" alt="" style="width: 56px; height: 56px; border-radius: 50%; object-fit: cover; border: 2px solid var(--primary);">`
                            : '';
                        return `
                            <div class="card" style="padding: 1.5rem; display: flex; flex-direction: column; justify-content: space-between;">
                                <div>
                                    <div style="display: flex; gap: 1rem; align-items: center; margin-bottom: 1rem;">
                                        ${profileImage}
                                        <div>
                                            <h3 style="font-size: 1.1rem; margin-bottom: 0.2rem;">${this.escapeHtml(f.name)}</h3>
                                            <p style="font-size: 0.78rem; color: var(--text-muted);">${this.escapeHtml(f.headline || 'Developer')}</p>
                                        </div>
                                    </div>

                                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; font-size: 0.82rem;">
                                        <span style="color: #fbbf24; font-weight: 700;"><i class="fa-solid fa-star"></i> ${f.rating || 4.9}</span>
                                        <span><i class="fa-solid fa-circle-check" style="color: var(--success);"></i> ${f.completedProjects || 0} completed</span>
                                        <span style="font-weight: 700;">₹${Number(f.hourlyRate || 1000).toLocaleString()}/hr</span>
                                    </div>

                                    <p class="project-desc" style="margin-bottom: 1rem;">${this.escapeHtml(f.bio || 'Experienced engineering specialist with demonstrated success across enterprise projects.')}</p>

                                    <div class="skills-wrapper">
                                        ${skillsHtml}
                                    </div>
                                </div>

                                <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-color); padding-top: 1rem; margin-top: 1rem;">
                                    <span style="font-size: 0.78rem; color: var(--text-muted);"><i class="fa-solid fa-location-dot"></i> ${this.escapeHtml(f.location || 'Remote')}</span>
                                    <button class="btn btn-outline btn-sm" onclick="app.navigate('profile', new URLSearchParams({ id: ${f.userId} }))">
                                        View Profile
                                    </button>
                                </div>
                            </div>
                        `;
                    }).join('');
                }
            }
        } catch (err) {
            if (container) {
                container.innerHTML = `<div style="grid-column: 1/-1; color: var(--danger); text-align: center;">Error: ${err.message}</div>`;
            }
        }
    }

    // ========================================================
    // USER PROFILE VIEW (Section 18)
    // ========================================================
    async loadUserProfile(userId) {
        const container = document.getElementById('profile-page-content');
        container.innerHTML = '<div style="text-align: center; padding: 4rem;"><i class="fa-solid fa-spinner fa-spin fa-2x"></i></div>';

        try {
            // Get user by id
            const flRes = await this.fetchApi(`/freelancers/user/${userId}`, 'GET');
            if (flRes.success && flRes.data) {
                const f = flRes.data;
                const skillsHtml = (f.skills || []).map(s => `<span class="skill-pill" style="padding: 0.35rem 0.85rem; font-size: 0.85rem;">${this.escapeHtml(s.name)}</span>`).join('');
                const profileImage = f.profileImage
                    ? `<img src="${this.escapeHtml(f.profileImage)}" alt="" style="width: 110px; height: 110px; border-radius: 50%; object-fit: cover; border: 4px solid var(--primary); box-shadow: var(--shadow-glow);">`
                    : '';

                // Load reviews
                let reviewsHtml = '';
                try {
                    const revRes = await this.fetchApi(`/reviews/user/${userId}`, 'GET');
                    if (revRes.success && revRes.data && revRes.data.length > 0) {
                        reviewsHtml = revRes.data.map(r => `
                            <div style="background: var(--bg-card-subtle); padding: 1.25rem; border-radius: var(--radius-md); border: 1px solid var(--border-color); margin-bottom: 1rem;">
                                <div style="display: flex; justify-content: space-between; margin-bottom: 0.35rem;">
                                    <strong>${this.escapeHtml(r.reviewerName)} <span style="font-size: 0.75rem; color: var(--text-muted);">(${r.reviewerRole})</span></strong>
                                    <div style="color: #fbbf24;">${'★'.repeat(r.rating)}${'☆'.repeat(5 - r.rating)}</div>
                                </div>
                                <p style="font-size: 0.85rem; color: var(--text-secondary);">${this.escapeHtml(r.comment)}</p>
                            </div>
                        `).join('');
                    } else {
                        reviewsHtml = '<p style="color: var(--text-muted); font-size: 0.85rem;">No client reviews yet.</p>';
                    }
                } catch (e) {}

                container.innerHTML = `
                    <div class="card" style="padding: 2.5rem; margin-bottom: 2rem;">
                        <div style="display: flex; gap: 2rem; align-items: center; flex-wrap: wrap;">
                            ${profileImage}
                            <div>
                                <h1 style="font-size: 2rem; margin-bottom: 0.25rem;">${this.escapeHtml(f.name)}</h1>
                                <p style="font-size: 1.1rem; color: var(--primary); font-weight: 600; margin-bottom: 0.5rem;">${this.escapeHtml(f.headline)}</p>
                                <div style="display: flex; gap: 1.5rem; font-size: 0.88rem; color: var(--text-muted); flex-wrap: wrap;">
                                    <span><i class="fa-solid fa-location-dot"></i> ${this.escapeHtml(f.location || 'Remote')}</span>
                                    <span><i class="fa-solid fa-star" style="color: #fbbf24;"></i> <strong>${f.rating || 5.0}</strong> rating</span>
                                    <span><i class="fa-solid fa-briefcase"></i> <strong>${f.completedProjects || 0}</strong> projects completed</span>
                                    <span><i class="fa-solid fa-indian-rupee-sign"></i> <strong>₹${Number(f.hourlyRate || 1000).toLocaleString()}/hr</strong></span>
                                </div>
                            </div>
                        </div>

                        <div style="margin-top: 2rem; padding-top: 1.5rem; border-top: 1px solid var(--border-color);">
                            <h3 style="font-size: 1.15rem; margin-bottom: 0.75rem;">About</h3>
                            <p style="font-size: 0.95rem; line-height: 1.7; color: var(--text-secondary);">${this.escapeHtml(f.bio || 'Experienced engineering specialist.')}</p>
                        </div>

                        <div style="margin-top: 2rem;">
                            <h3 style="font-size: 1.15rem; margin-bottom: 0.75rem;">Technical Skills</h3>
                            <div class="skills-wrapper">${skillsHtml}</div>
                        </div>

                        <div style="margin-top: 2rem;">
                            <h3 style="font-size: 1.15rem; margin-bottom: 0.75rem;">Portfolio & Links</h3>
                            <div style="display: flex; gap: 1rem; flex-wrap: wrap;">
                                ${f.githubUrl ? `<a href="${this.escapeHtml(f.githubUrl)}" target="_blank" class="btn btn-outline btn-sm"><i class="fa-brands fa-github"></i> GitHub</a>` : ''}
                                ${f.linkedinUrl ? `<a href="${this.escapeHtml(f.linkedinUrl)}" target="_blank" class="btn btn-outline btn-sm"><i class="fa-brands fa-linkedin"></i> LinkedIn</a>` : ''}
                                ${f.portfolioUrl ? `<a href="${this.escapeHtml(f.portfolioUrl)}" target="_blank" class="btn btn-outline btn-sm"><i class="fa-solid fa-globe"></i> Website</a>` : ''}
                            </div>
                        </div>
                    </div>

                    <div class="card" style="padding: 2rem;">
                        <h3 style="font-size: 1.15rem; margin-bottom: 1.25rem;"><i class="fa-solid fa-star" style="color: #fbbf24;"></i> Client Testimonials</h3>
                        ${reviewsHtml}
                    </div>
                `;
            }
        } catch (err) {
            container.innerHTML = `<div style="color: var(--danger); text-align: center;">Error loading profile: ${err.message}</div>`;
        }
    }

    // ========================================================
    // MESSAGING SYSTEM (Section 21)
    // ========================================================
    async openChatModal(projectId, projectTitle) {
        this.currentProjectId = projectId;
        document.getElementById('chat-modal-title').textContent = 'Project Collaboration: ' + projectTitle;
        this.showModal('messages-modal');
        this.loadChatMessages(projectId);

        // Auto poll messages every 4 seconds while modal is open
        if (this.chatPollInterval) clearInterval(this.chatPollInterval);
        this.chatPollInterval = setInterval(() => {
            if (document.getElementById('messages-modal').style.display === 'flex') {
                this.loadChatMessages(projectId, false);
            }
        }, 4000);
    }

    async loadChatMessages(projectId, scrollBottom = true) {
        try {
            const res = await this.fetchApi(`/messages/project/${projectId}`, 'GET');
            const container = document.getElementById('chat-messages-container');
            if (!container) return;

            if (res.success && res.data) {
                if (res.data.length === 0) {
                    container.innerHTML = '<div style="text-align: center; color: var(--text-muted); margin: auto;">No messages exchanged yet. Start the conversation!</div>';
                } else {
                    container.innerHTML = res.data.map(m => {
                        const isSent = this.currentUser && m.senderId === this.currentUser.userId;
                        return `
                            <div class="chat-bubble ${isSent ? 'sent' : 'received'}">
                                <div style="font-weight: 700; font-size: 0.75rem; margin-bottom: 0.2rem; opacity: 0.85;">${this.escapeHtml(m.senderName)}</div>
                                <div>${this.escapeHtml(m.message)}</div>
                                <div class="chat-time">${(m.sentAt || '').split('T')[1]?.substring(0, 5) || ''}</div>
                            </div>
                        `;
                    }).join('');

                    if (scrollBottom) {
                        container.scrollTop = container.scrollHeight;
                    }
                }
            }
        } catch (err) {
            console.error('Error loading chat messages:', err);
        }
    }

    async sendChatMessage() {
        const input = document.getElementById('chat-input');
        const text = input.value.trim();
        if (!text) return;

        // Determine recipient
        try {
            const projRes = await this.fetchApi(`/projects/${this.currentProjectId}`, 'GET');
            if (!projRes.success || !projRes.data) return;

            const p = projRes.data;
            let receiverId;

            if (this.currentUser.role === 'CLIENT') {
                // Find freelancer user id
                const flRes = await this.fetchApi(`/freelancers/${p.assignedFreelancerId}`, 'GET');
                receiverId = flRes.data?.userId;
            } else {
                // Freelancer to client
                // Find client user id
                receiverId = p.clientId;
            }

            if (!receiverId) {
                receiverId = 1; // fallback
            }

            const payload = {
                senderId: this.currentUser.userId,
                receiverId: receiverId,
                projectId: this.currentProjectId,
                message: text
            };

            input.value = '';
            const res = await this.fetchApi('/messages', 'POST', payload);
            if (res.success) {
                this.loadChatMessages(this.currentProjectId, true);
            }
        } catch (err) {
            this.showToast('Failed to send message: ' + err.message, 'error');
        }
    }

    // ========================================================
    // REVIEWS AND RATINGS (Section 19)
    // ========================================================
    initStarRating() {
        const stars = document.querySelectorAll('#review-stars-container .star');
        const ratingInput = document.getElementById('review-rating-val');

        stars.forEach(star => {
            star.addEventListener('click', (e) => {
                const rating = parseInt(e.target.dataset.rating, 10);
                ratingInput.value = rating;
                stars.forEach(s => {
                    const r = parseInt(s.dataset.rating, 10);
                    if (r <= rating) {
                        s.classList.add('active');
                    } else {
                        s.classList.remove('active');
                    }
                });
            });
        });
    }

    async openReviewModal(projectId) {
        if (!this.currentUser) return;
        document.getElementById('review-project-id').value = projectId;

        // Determine who to review
        const projRes = await this.fetchApi('/projects/' + projectId, 'GET');
        if (!projRes.success) return;

        const p = projRes.data;
        let targetId;
        let targetName;

        if (this.currentUser.role === 'CLIENT') {
            const flRes = await this.fetchApi(`/freelancers/${p.assignedFreelancerId}`, 'GET');
            targetId = flRes.data?.userId;
            targetName = flRes.data?.name || 'Freelancer';
        } else {
            targetId = 7; // Client user ID
            targetName = p.clientCompanyName || 'Client';
        }

        document.getElementById('review-reviewee-id').value = targetId;
        document.getElementById('review-modal-target-name').textContent = 'For: ' + targetName;
        document.getElementById('review-comment').value = '';
        this.showModal('review-modal');
    }

    async handleSubmitReview(e) {
        e.preventDefault();
        const projectId = document.getElementById('review-project-id').value;
        const revieweeId = document.getElementById('review-reviewee-id').value;
        const rating = parseInt(document.getElementById('review-rating-val').value, 10);
        const comment = document.getElementById('review-comment').value;

        const payload = {
            projectId,
            reviewerId: this.currentUser.userId,
            revieweeId,
            rating,
            comment
        };

        try {
            const res = await this.fetchApi('/reviews', 'POST', payload);
            if (res.success) {
                this.closeModal('review-modal');
                this.showToast('Thank you! Your review has been submitted.', 'success');
                this.loadProjectDetails(projectId);
            } else {
                this.showToast(res.message || 'Review submission failed', 'error');
            }
        } catch (err) {
            this.showToast(err.message || 'Error submitting review', 'error');
        }
    }

    // ========================================================
    // NOTIFICATIONS (Section 20)
    // ========================================================
    async loadNotificationCount() {
        if (!this.currentUser) return;

        try {
            const res = await this.fetchApi(`/notifications/${this.currentUser.userId}/unread-count`, 'GET');
            const badge = document.getElementById('notif-badge');
            if (res.success && res.data) {
                const count = res.data.unreadCount || 0;
                if (count > 0) {
                    badge.textContent = count > 9 ? '9+' : count;
                    badge.style.display = 'flex';
                } else {
                    badge.style.display = 'none';
                }
            }
        } catch (e) {
            // Fail silently on polling
        }
    }

    async loadNotificationsList() {
        if (!this.currentUser) return;

        try {
            const res = await this.fetchApi(`/notifications/${this.currentUser.userId}`, 'GET');
            const list = document.getElementById('notif-list');
            if (!list) return;

            if (res.success && res.data) {
                if (res.data.length === 0) {
                    list.innerHTML = '<div style="text-align: center; padding: 2rem; color: var(--text-muted); font-size: 0.85rem;">No notifications yet.</div>';
                } else {
                    list.innerHTML = res.data.map(n => `
                        <div class="notif-item ${!n.isRead ? 'unread' : ''}" onclick="app.markNotificationRead(${n.id})">
                            <div class="notif-icon"><i class="fa-solid fa-bell"></i></div>
                            <div class="notif-content">
                                <div class="notif-title">${this.escapeHtml(n.title)}</div>
                                <div class="notif-msg">${this.escapeHtml(n.message)}</div>
                                <div class="notif-time">${(n.createdAt || '').replace('T', ' ')}</div>
                            </div>
                        </div>
                    `).join('');
                }
            }
        } catch (e) {
            console.error('Error loading notifications:', e);
        }
    }

    async markNotificationRead(id) {
        try {
            await this.fetchApi(`/notifications/${id}/read`, 'PUT');
            this.loadNotificationCount();
            this.loadNotificationsList();
        } catch (e) {}
    }

    async markAllNotificationsRead() {
        if (!this.currentUser) return;
        try {
            await this.fetchApi(`/notifications/user/${this.currentUser.userId}/read-all`, 'PUT');
            this.loadNotificationCount();
            this.loadNotificationsList();
            this.showToast('All notifications marked as read', 'info');
        } catch (e) {}
    }

    // ========================================================
    // UI DROPDOWNS & MODALS HELPERS
    // ========================================================
    initDropdowns() {
        const notifBtn = document.getElementById('notif-btn');
        const notifDropdown = document.getElementById('notif-dropdown');
        const userBtn = document.getElementById('user-dropdown-btn');
        const userDropdown = document.getElementById('user-dropdown');
        const demoBtn = document.getElementById('demo-role-btn');

        if (notifBtn) {
            notifBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                const isOpen = notifDropdown.classList.contains('show');
                this.closeAllDropdowns();
                if (!isOpen) {
                    notifDropdown.classList.add('show');
                    this.loadNotificationsList();
                }
            });
        }

        if (userBtn) {
            userBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                const isOpen = userDropdown.classList.contains('show');
                this.closeAllDropdowns();
                if (!isOpen) userDropdown.classList.add('show');
            });
        }

        if (demoBtn) {
            demoBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                this.showModal('login-modal');
            });
        }

        document.addEventListener('click', () => {
            this.closeAllDropdowns();
        });
    }

    closeAllDropdowns() {
        document.querySelectorAll('.dropdown-menu').forEach(m => m.classList.remove('show'));
    }

    showModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) modal.classList.add('show');
    }

    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) modal.classList.remove('show');
    }

    showToast(message, type = 'info') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;

        let icon = 'fa-info-circle';
        if (type === 'success') icon = 'fa-circle-check';
        else if (type === 'error') icon = 'fa-circle-xmark';
        else if (type === 'warning') icon = 'fa-triangle-exclamation';

        toast.innerHTML = `
            <i class="fa-solid ${icon} toast-icon"></i>
            <span class="toast-message">${this.escapeHtml(message)}</span>
        `;

        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    }

    escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    // ========================================================
    // HTTP FETCH CLIENT
    // ========================================================
    async fetchApi(endpoint, method = 'GET', body = null) {
        const options = {
            method,
            headers: {
                'Content-Type': 'application/json'
            }
        };

        if (this.currentUser && this.currentUser.token) {
            options.headers['Authorization'] = 'Bearer ' + this.currentUser.token;
        }

        if (body && (method === 'POST' || method === 'PUT')) {
            options.body = JSON.stringify(body);
        }

        const res = await fetch(API_BASE + endpoint, options);
        const contentType = res.headers.get('content-type') || '';
        if (!contentType.includes('application/json')) {
            if (!res.ok) {
                throw new Error(`Backend API is currently unreachable (${res.status} ${res.statusText}). If you deployed to Vercel, please connect your deployed Spring Boot backend URL.`);
            }
            throw new Error(`Server returned unexpected response type (${contentType || 'non-JSON'}).`);
        }
        const data = await res.json();

        if (!res.ok && !data.message) {
            throw new Error(`HTTP Error ${res.status}`);
        }

        return data;
    }
}

// Global Application Instance
let app;
document.addEventListener('DOMContentLoaded', () => {
    app = new FreelanceHubApp();
});
