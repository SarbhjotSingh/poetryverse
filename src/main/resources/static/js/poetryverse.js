/**
 * ============================================================================
 * POETRYVERSE — CLIENT-SIDE INTERACTION & REST API ENGINE
 * ============================================================================
 */

window.PoetryVerse = {

    showToast: function (message, type = 'success') {
        let container = document.getElementById('poetryToastContainer');
        if (!container) {
            container = document.createElement('div');
            container.id = 'poetryToastContainer';
            container.className = 'poetry-toast-container';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = 'poetry-toast';
        if (type === 'error') {
            toast.style.borderLeftColor = '#C95B5B';
        }
        const icon = type === 'error' ? 'fa-circle-exclamation' : 'fa-feather-pointed';
        toast.innerHTML = `
            <i class="fa-solid ${icon}" style="color: #C8B276; font-size: 16px;"></i>
            <div style="flex: 1;">${message}</div>
        `;
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(20px)';
            toast.style.transition = 'all 0.25s ease';
            setTimeout(() => toast.remove(), 260);
        }, 3600);
    },

    toggleSidebarMobile: function () {
        const sb = document.querySelector('.workspace-sidebar');
        if (sb) sb.classList.toggle('mobile-open');
    },

    toggleFavorite: async function (poemId, btnEl) {
        try {
            const res = await fetch(`/api/poems/${poemId}/favorite`, { method: 'POST' });
            const data = await res.json();
            if (data.success) {
                if (btnEl) {
                    const icon = btnEl.querySelector('i');
                    if (icon) {
                        if (data.favorite) {
                            icon.classList.remove('fa-regular');
                            icon.classList.add('fa-solid');
                            icon.style.color = '#A18B50';
                        } else {
                            icon.classList.remove('fa-solid');
                            icon.classList.add('fa-regular');
                            icon.style.color = '';
                        }
                    }
                }
                this.showToast(data.message);
            } else {
                this.showToast(data.message || 'Could not update favorite status.', 'error');
            }
        } catch (err) {
            this.showToast('Network error while updating favorite.', 'error');
        }
    },

    duplicatePoem: async function (poemId) {
        try {
            const res = await fetch(`/api/poems/${poemId}/duplicate`, { method: 'POST' });
            const data = await res.json();
            if (data.success) {
                this.showToast(data.message);
                setTimeout(() => window.location.reload(), 700);
            } else {
                this.showToast(data.message || 'Failed to duplicate poem.', 'error');
            }
        } catch (err) {
            this.showToast('Error duplicating manuscript.', 'error');
        }
    },

    toggleArchive: async function (poemId) {
        try {
            const res = await fetch(`/api/poems/${poemId}/archive`, { method: 'POST' });
            const data = await res.json();
            if (data.success) {
                this.showToast(data.message);
                setTimeout(() => window.location.reload(), 650);
            } else {
                this.showToast(data.message || 'Failed to archive poem.', 'error');
            }
        } catch (err) {
            this.showToast('Error updating archive state.', 'error');
        }
    },

    deletePoem: async function (poemId, poemTitle) {
        const titleText = poemTitle || 'this manuscript';
        if (!confirm(`Are you certain you wish to permanently remove "${titleText}" from your private library?`)) {
            return;
        }
        try {
            const res = await fetch(`/api/poems/${poemId}`, { method: 'DELETE' });
            const data = await res.json();
            if (data.success) {
                this.showToast(data.message);
                setTimeout(() => {
                    if (window.location.pathname.startsWith('/editor')) {
                        window.location.href = '/library';
                    } else {
                        window.location.reload();
                    }
                }, 650);
            } else {
                this.showToast(data.message || 'Could not delete poem.', 'error');
            }
        } catch (err) {
            this.showToast('Error deleting manuscript.', 'error');
        }
    },

    openManuscriptModal: async function (poemId) {
        try {
            const res = await fetch(`/api/poems/${poemId}`);
            if (!res.ok) throw new Error('Failed to load poem');
            const p = await res.json();

            const modalEl = document.getElementById('manuscriptReaderModal');
            if (!modalEl) {
                window.location.href = `/editor/${poemId}`;
                return;
            }

            document.getElementById('readerPoemTitle').textContent = p.title || 'Untitled Verse';
            const subEl = document.getElementById('readerPoemSubtitle');
            if (subEl) {
                subEl.textContent = p.subtitle || '';
                subEl.style.display = p.subtitle ? 'block' : 'none';
            }

            const verseEl = document.getElementById('readerPoemContent');
            verseEl.textContent = p.content || '';
            verseEl.style.fontFamily = p.fontFamily ? `'${p.fontFamily}', serif` : "'Cormorant Garamond', serif";
            verseEl.style.textAlign = (p.textAlign || 'LEFT').toLowerCase();

            document.getElementById('readerPoemTitle').style.textAlign = (p.textAlign || 'LEFT').toLowerCase();
            if (subEl) subEl.style.textAlign = (p.textAlign || 'LEFT').toLowerCase();

            document.getElementById('readerPoemGenre').textContent = p.genre || 'Free Verse';
            document.getElementById('readerPoemMood').textContent = p.mood || 'Contemplative';
            document.getElementById('readerPoemStats').textContent =
                `${p.wordCount || 0} words • ${p.lineCount || 0} lines • ${p.stanzaCount || 1} stanzas`;

            const editLink = document.getElementById('readerEditLink');
            if (editLink) editLink.href = `/editor/${p.id}`;
            const pdfLink = document.getElementById('readerPdfLink');
            if (pdfLink) pdfLink.href = `/api/poems/${p.id}/export/pdf`;
            const docxLink = document.getElementById('readerDocxLink');
            if (docxLink) docxLink.href = `/api/poems/${p.id}/export/docx`;
            const txtLink = document.getElementById('readerTxtLink');
            if (txtLink) txtLink.href = `/api/poems/${p.id}/export/txt`;

            const bsModal = new bootstrap.Modal(modalEl);
            bsModal.show();
        } catch (err) {
            this.showToast('Unable to open manuscript reader.', 'error');
        }
    },

    printCurrentModalPoem: function () {
        const title = document.getElementById('readerPoemTitle')?.textContent || 'Manuscript';
        const subtitle = document.getElementById('readerPoemSubtitle')?.textContent || '';
        const content = document.getElementById('readerPoemContent')?.textContent || '';
        const meta = document.getElementById('readerPoemStats')?.textContent || '';

        const win = window.open('', '_blank', 'width=820,height=900');
        win.document.write(`
            <!DOCTYPE html>
            <html>
            <head>
                <title>${title} — PoetryVerse Print Edition</title>
                <link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,400;0,600;1,400&display=swap" rel="stylesheet">
                <style>
                    body {
                        background: #FAF7F0;
                        color: #25251E;
                        font-family: 'Cormorant Garamond', Georgia, serif;
                        padding: 60px;
                        margin: 0;
                    }
                    .print-frame {
                        border: 2px solid #A18B50;
                        outline: 1px solid #A18B50;
                        outline-offset: -8px;
                        padding: 56px 64px;
                        max-width: 640px;
                        margin: 0 auto;
                    }
                    h1 { font-size: 36px; margin: 0 0 6px; color: #353626; font-weight: 600; }
                    h2 { font-size: 19px; font-style: italic; color: #60604D; margin: 0 0 28px; font-weight: 400; }
                    pre {
                        font-family: 'Cormorant Garamond', Georgia, serif;
                        font-size: 20px;
                        line-height: 1.85;
                        white-space: pre-wrap;
                        margin: 24px 0 36px;
                    }
                    .colophon {
                        border-top: 1px solid rgba(161,139,80,0.4);
                        padding-top: 14px;
                        font-size: 13px;
                        color: #60604D;
                        font-style: italic;
                    }
                </style>
            </head>
            <body>
                <div class="print-frame">
                    <div style="font-size:11px; letter-spacing:0.2em; color:#A18B50; text-transform:uppercase; margin-bottom:12px;">
                        PoetryVerse Classical Print Edition
                    </div>
                    <h1>${title}</h1>
                    ${subtitle ? `<h2>${subtitle}</h2>` : ''}
                    <pre>${content}</pre>
                    <div class="colophon">${meta} • Printed from PoetryVerse</div>
                </div>
                <script>
                    window.onload = function() { setTimeout(function(){ window.print(); }, 350); };
                <\/script>
            </body>
            </html>
        `);
        win.document.close();
    },

    inspireMe: async function () {
        try {
            const res = await fetch('/api/discover/inspire');
            const p = await res.json();
            const titleEl = document.getElementById('inspirePromptTitle');
            const textEl = document.getElementById('inspirePromptText');
            const sampleEl = document.getElementById('inspirePromptSample');
            const genreEl = document.getElementById('inspirePromptGenre');
            const startBtn = document.getElementById('inspireWriteBtn');

            if (titleEl) titleEl.textContent = p.title;
            if (textEl) textEl.textContent = p.promptText;
            if (sampleEl) sampleEl.textContent = p.sampleVerse || '';
            if (genreEl) genreEl.textContent = `${p.suggestedGenre || 'Free Verse'} • ${p.suggestedMood || 'Contemplative'}`;
            if (startBtn && p.id) startBtn.href = `/editor?promptId=${p.id}`;

            const modalEl = document.getElementById('inspireMeModal');
            if (modalEl) {
                const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
                modal.show();
            } else {
                this.showToast(`Prompt: "${p.title}" — ${p.promptText}`);
            }
        } catch (err) {
            this.showToast('Could not fetch writing prompt.', 'error');
        }
    }
};
