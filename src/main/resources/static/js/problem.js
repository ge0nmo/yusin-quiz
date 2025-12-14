const problemApp = {
    selectedSubjectId: null,
    selectedSubjectName: null,
    selectedYear: null,
    selectedExamId: null,
    selectedExamName: null,

    // Summernote 설정
    summernoteOption: {
        height: 400,
        lang: 'ko-KR',
        toolbar: [
            ['style', ['bold', 'italic', 'underline', 'clear']],
            ['font', ['strikethrough', 'superscript', 'subscript', 'color']],
            ['fontsize', ['fontsize']],
            ['para', ['ul', 'ol', 'paragraph']],
            ['table', ['table']],
            ['insert', ['link', 'picture', 'hr']],
            ['view', ['codeview', 'help']]
        ],
        callbacks: {
            onImageUpload: function(files) {
                // 다중 파일 업로드 지원
                for (let i = 0; i < files.length; i++) {
                    problemApp.uploadImageToServer(files[i], $(this));
                }
            },
            onPaste: function(e) {
                var clipboardData = (e.originalEvent || e).clipboardData;
                if (clipboardData && clipboardData.items && clipboardData.items.length) {
                    var item = clipboardData.items[0];
                    if (item.kind === 'file' && item.type.indexOf('image/') !== -1) {
                        e.preventDefault();
                        var file = item.getAsFile();
                        problemApp.uploadImageToServer(file, $(this));
                    }
                }
            }
        }
    },

    async init() {
        await new Promise(resolve => {
            const check = () => window.jQuery && jQuery.fn.summernote ? resolve() : setTimeout(check, 100);
            check();
        });

        this.initEventListeners();
        await this.loadSubjects();

        // [추가] 2. 세션 스토리지 확인 및 자동 로딩 로직 (Exam 페이지에서 넘어온 경우)
        const storedSubjectId = sessionStorage.getItem("subjectId");
        const storedSubjectName = sessionStorage.getItem("subjectName");
        const storedExamId = sessionStorage.getItem("examId");
        const storedExamName = sessionStorage.getItem("examName");
        const storedExamYear = sessionStorage.getItem("examYear");

        // 과목 정보가 있으면 과목 선택 상태로 세팅
        if (storedSubjectId && storedSubjectName) {
            await this.selectSubject(storedSubjectId, storedSubjectName);

            // 시험 정보와 연도 정보까지 있으면 문제 목록 자동 조회
            if (storedExamId && storedExamYear) {
                // 2-1. 연도 선택 (내부적으로 loadExams 호출됨)
                await this.selectYear(storedExamYear);

                // 2-2. 시험 드롭다운 값 강제 세팅 (UI 동기화)
                const examSelect = document.getElementById('examList');
                if(examSelect) {
                    examSelect.value = storedExamId;
                }

                // 2-3. 내부 상태값 세팅
                this.selectedExamId = storedExamId;
                this.selectedExamName = storedExamName;

                // 2-4. 문제 조회 실행
                this.searchProblems();

                // (선택사항) 자동 조회 후 세션 스토리지의 시험 정보 삭제
                // sessionStorage.removeItem("examId");
                // sessionStorage.removeItem("examName");
                // sessionStorage.removeItem("examYear");
            }
        }
    },

    initEventListeners() {
        document.querySelector('.search-click').addEventListener('click', () => this.searchProblems());
        document.querySelector('.add-problem-button').addEventListener('click', () => this.openCreateModal());
        document.querySelector('.problem-save-button').addEventListener('click', () => this.saveProblem());
        document.getElementById('addChoiceBtn').addEventListener('click', () => this.addChoiceRow());
        document.getElementById('examList').addEventListener('change', (e) => {
            this.selectedExamId = e.target.value;
            this.selectedExamName = e.target.options[e.target.selectedIndex].text;
        });
    },

    // --- 1. 필터 데이터 로딩 ---
    async loadSubjects() {
        try {
            const subjects = await this.getJSON("/admin/subject/list");
            const dropdown = document.getElementById('subject-dropdown-content');
            dropdown.innerHTML = '';

            subjects.forEach(sub => {
                const li = document.createElement('li');
                const btn = document.createElement('a');
                btn.className = 'dropdown-item';
                btn.href = '#';
                btn.textContent = sub.name;
                btn.onclick = (e) => {
                    e.preventDefault();
                    this.selectSubject(sub.id, sub.name);
                };
                li.appendChild(btn);
                dropdown.appendChild(li);
            });
        } catch(e) { console.error("과목 로딩 실패", e); }
    },

    async selectSubject(id, name) {
        this.selectedSubjectId = id;
        this.selectedSubjectName = name;
        document.getElementById('subject-dropdown').textContent = name;

        this.selectedYear = null;
        this.selectedExamId = null;
        document.getElementById('year-dropdown').textContent = '연도 선택';
        document.getElementById('year-content').innerHTML = '';
        document.getElementById('examList').innerHTML = '<option value="">시험을 선택하세요</option>';
        document.getElementById('examTable').innerHTML = '';

        await this.loadYears();
    },

    async loadYears() {
        if (!this.selectedSubjectId) return;
        try {
            const years = await this.getJSON(`/admin/exam/year?subjectId=${this.selectedSubjectId}`);
            const dropdown = document.getElementById('year-content');
            dropdown.innerHTML = '';

            years.forEach(year => {
                const li = document.createElement('li');
                const btn = document.createElement('a');
                btn.className = 'dropdown-item';
                btn.href = '#';
                btn.textContent = `${year}년`;
                btn.onclick = (e) => {
                    e.preventDefault();
                    this.selectYear(year);
                };
                li.appendChild(btn);
                dropdown.appendChild(li);
            });
        } catch(e) { console.error("연도 로딩 실패", e); }
    },

    async selectYear(year) {
        this.selectedYear = year;
        document.getElementById('year-dropdown').textContent = `${year}년`;
        await this.loadExams();
    },

    async loadExams() {
        try {
            const exams = await this.getJSON(`/admin/subject/${this.selectedSubjectId}/exam?year=${this.selectedYear}`);
            const select = document.getElementById('examList');
            select.innerHTML = '<option value="">시험을 선택하세요</option>';
            exams.forEach(e => {
                const option = document.createElement('option');
                option.value = e.id;
                option.textContent = e.name;
                select.appendChild(option);
            });
        } catch(e) { console.error("시험 로딩 실패", e); }
    },

    // --- 2. 문제 목록 ---
    async searchProblems() {
        if(!this.selectedExamId) return alert("시험을 선택해주세요.");
        try {
            const problems = await this.getJSON(`/admin/problem/list?examId=${this.selectedExamId}`);
            this.renderProblems(problems);
        } catch(e) {
            console.error(e);
            document.getElementById('examTable').innerHTML = '<div class="text-center text-danger">데이터 로드 실패</div>';
        }
    },

    renderProblems(list) {
        const container = document.getElementById('examTable');
        // 응답 구조에 따라 list가 { data: [...] } 형태일 수 있음. 확인 필요.
        // 기존 코드에서는 바로 list 매핑 중이었으므로 그대로 유지.
        // 만약 GlobalResponse를 사용한다면 list.data로 접근해야 함.
        const problems = list.data || list;

        if(!problems || problems.length === 0) {
            container.innerHTML = `<div class="text-center py-5 w-100 text-muted">등록된 문제가 없습니다.</div>`;
            return;
        }
        container.innerHTML = problems.map(p => this.createCardHTML(p)).join('');
    },

    createCardHTML(problem) {
        const strip = (html) => {
            let tmp = document.createElement("DIV");
            tmp.innerHTML = html;
            return (tmp.textContent || tmp.innerText || "").substring(0, 100);
        };
        const contentPreview = strip(problem.content);
        const dataJson = JSON.stringify(problem).replace(/"/g, '&quot;');

        const choicesHtml = (problem.choices || []).map(c =>
            `<div class="d-flex align-items-start mb-2 ${c.isAnswer ? 'text-success fw-bold' : 'text-secondary'}">
                <span class="badge ${c.isAnswer ? 'bg-success' : 'bg-light text-dark border'} me-2 mt-1" style="min-width: 24px;">${c.number}</span>
                <span class="choice-text-wrap">${c.content}</span>
             </div>`
        ).join('');

        return `
        <div class="problem-card" onclick='problemApp.openEditModal(${dataJson})'>
            <div class="problem-header d-flex justify-content-between align-items-center">
                <span class="badge bg-primary">No. ${problem.number}</span>
                <button class="btn btn-sm btn-link text-danger p-0" onclick="event.stopPropagation(); problemApp.deleteProblem(${problem.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
            <div class="card-body p-3">
                <div class="mb-3" style="min-height: 40px;">${contentPreview}...</div>
                <div class="choices-preview border-top pt-3">
                    ${choicesHtml}
                </div>
            </div>
        </div>
        `;
    },

    // --- 3. 모달 제어 (getOrCreateInstance 사용) ---
    openCreateModal() {
        if(!this.selectedExamId) return alert("시험을 먼저 선택해주세요.");

        this.resetModal('새 문제 등록', 'create');

        document.getElementById('subjectTitle').value = this.selectedSubjectName;
        document.getElementById('examName').value = this.selectedExamName;
        document.getElementById('examYear').value = `${this.selectedYear}년`;

        for(let i=1; i<=5; i++) this.addChoiceRow({number: i, content: '', isAnswer: false});

        bootstrap.Modal.getOrCreateInstance(document.getElementById('add-problem-modal')).show();
    },

    openEditModal(problem) {
        this.resetModal('문제 수정', 'edit', problem.id);

        document.getElementById('subjectTitle').value = this.selectedSubjectName;
        document.getElementById('examName').value = this.selectedExamName;
        document.getElementById('examYear').value = `${this.selectedYear}년`;

        document.getElementById('problemNumber').value = problem.number;
        $('#problemContent').summernote('code', problem.content);
        $('#problemExplanation').summernote('code', problem.explanation);

        (problem.choices || []).forEach(c => this.addChoiceRow(c));

        bootstrap.Modal.getOrCreateInstance(document.getElementById('add-problem-modal')).show();
    },

    resetModal(title, mode, id = null) {
        const modal = document.getElementById('add-problem-modal');
        document.getElementById('modalTitle').textContent = title;
        modal.setAttribute('data-mode', mode);
        modal.setAttribute('data-id', id || '');

        document.getElementById('problemNumber').value = '';
        $('#problemContent').summernote('code', '');
        $('#problemExplanation').summernote('code', '');
        document.getElementById('choicesContainer').innerHTML = '';
    },

    addChoiceRow(data = null) {
        const index = document.querySelectorAll('.choice-row').length + 1;
        const number = data ? data.number : index;
        const content = data ? data.content : '';
        const isAnswer = data ? data.isAnswer : false;
        const idAttr = (data && data.id) ? `data-id="${data.id}"` : '';

        const html = `
            <div class="choice-row input-group mb-2" ${idAttr}>
                <div class="input-group-text bg-white">
                    <input class="form-check-input mt-0 isAnswer" type="radio" name="correctAnswer" ${isAnswer ? 'checked' : ''}>
                </div>
                <input type="number" class="form-control choice-number" style="max-width: 60px;" value="${number}">
                <input type="text" class="form-control choiceContent" value="${content}" placeholder="보기 내용">
                <button type="button" class="btn btn-outline-danger" onclick="this.closest('.choice-row').remove()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;
        document.getElementById('choicesContainer').insertAdjacentHTML('beforeend', html);
    },

    // --- 4. 저장 및 백드롭 해결 ---
    async saveProblem() {
        const modalEl = document.getElementById('add-problem-modal');
        const mode = modalEl.getAttribute('data-mode');
        const problemId = modalEl.getAttribute('data-id');

        const number = document.getElementById('problemNumber').value;
        const content = $('#problemContent').summernote('code');
        const explanation = $('#problemExplanation').summernote('code');

        const choices = [];
        let hasAnswer = false;
        document.querySelectorAll('.choice-row').forEach(row => {
            const cId = row.getAttribute('data-id');
            const cNum = row.querySelector('.choice-number').value;
            const cContent = row.querySelector('.choiceContent').value;
            const cIsAnswer = row.querySelector('.isAnswer').checked;

            if(cIsAnswer) hasAnswer = true;
            if(cContent.trim()) {
                choices.push({
                    id: (mode === 'edit' && cId) ? parseInt(cId) : null,
                    number: parseInt(cNum),
                    content: cContent,
                    isAnswer: cIsAnswer
                });
            }
        });

        if(!number) return alert("문제 번호를 입력하세요.");
        if($('#problemContent').summernote('isEmpty')) return alert("문제 지문을 입력하세요.");
        if(!hasAnswer) return alert("정답을 하나 이상 선택해주세요.");

        const payload = {
            id: mode === 'edit' ? parseInt(problemId) : null,
            examId: parseInt(this.selectedExamId),
            number: parseInt(number),
            content: content,
            explanation: explanation,
            choices: choices
        };

        try {
            const method = mode === 'edit' ? 'PATCH' : 'POST';
            const url = `/admin/problem?examId=${this.selectedExamId}`;

            const res = await fetch(url, {
                method: method,
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            });

            if(!res.ok) throw new Error("저장 실패");

            bootstrap.Modal.getOrCreateInstance(modalEl).hide();
            this.removeBackdrop();
            this.searchProblems();
            alert("저장되었습니다.");
        } catch(e) { console.error(e); alert("오류 발생"); }
    },

    removeBackdrop() {
        const backdrops = document.querySelectorAll('.modal-backdrop');
        backdrops.forEach(backdrop => backdrop.remove());
        document.body.classList.remove('modal-open');
        document.body.style.overflow = '';
        document.body.style.paddingRight = '';
    },

    async deleteProblem(id) {
        if(!confirm("정말 삭제하시겠습니까?")) return;
        try {
            const res = await fetch(`/admin/problem/${id}`, { method: 'DELETE' });
            if(res.ok) this.searchProblems();
            else alert("삭제 실패");
        } catch(e) { alert("오류 발생"); }
    },

    async getJSON(url) {
        const res = await fetch(url);
        if(!res.ok) throw new Error(res.status);
        return await res.json();
    },

    uploadImageToServer(file, editor) {
        const formData = new FormData();
        formData.append('file', file);
        fetch('/admin/file', { method: 'POST', body: formData })
            .then(r => r.ok ? r.text() : Promise.reject())
            .then(url => editor.summernote('insertImage', url))
            .catch(e => alert('이미지 업로드 실패'));
    }
};

window.problemApp = problemApp;
document.addEventListener('DOMContentLoaded', () => problemApp.init());