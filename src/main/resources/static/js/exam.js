const examApp = {
    selectedSubjectId: null,
    selectedSubjectName: null,
    selectedExamId: null,

    init() {
        this.selectedSubjectId = sessionStorage.getItem("subjectId");
        this.selectedSubjectName = sessionStorage.getItem("subjectName");

        this.loadSubjects();

        if (this.selectedSubjectId) {
            this.setSubjectUI(this.selectedSubjectId, this.selectedSubjectName);
            // 초기 로딩 시 연도 목록과 시험 목록을 둘 다 가져옵니다.
            this.loadYears();
            this.loadExams();
        }
    },

    // --- 1. 과목 관련 ---
    async loadSubjects() {
        try {
            const res = await fetch('/admin/subject/list');
            const subjects = await res.json();

            const listContainer = document.getElementById('subject-list-container');
            listContainer.innerHTML = subjects.map(sub =>
                `<li><a class="dropdown-item" href="#" onclick="examApp.changeSubject(${sub.id}, '${sub.name}')">${sub.name}</a></li>`
            ).join('');
        } catch(e) { console.error(e); }
    },

    changeSubject(id, name) {
        this.selectedSubjectId = id;
        this.selectedSubjectName = name;
        sessionStorage.setItem("subjectId", id);
        sessionStorage.setItem("subjectName", name);

        this.setSubjectUI(id, name);

        // 과목이 바뀌면 연도 필터 초기화 후 다시 로딩
        document.getElementById('yearFilter').value = "";
        this.loadYears();
        this.loadExams();
    },

    setSubjectUI(id, name) {
        document.getElementById('currentSubjectTitle').textContent = name;
        document.getElementById('subjectDropdownBtn').textContent = name;
        document.getElementById('yearFilter').disabled = false; // 연도 선택 활성화
    },

    // --- 2. 연도 목록 로딩 (API 활용) ---
    async loadYears() {
        if(!this.selectedSubjectId) return;

        try {
            // ExamController에 만들어둔 getYear API 활용 (/admin/exam/year?subjectId=...)
            const res = await fetch(`/admin/exam/year?subjectId=${this.selectedSubjectId}`);
            const years = await res.json();

            const yearSelect = document.getElementById('yearFilter');
            // '전체 연도' 옵션은 유지하고 나머지만 새로 그림
            yearSelect.innerHTML = '<option value="">전체 연도</option>' +
                years.map(y => `<option value="${y}">${y}년</option>`).join('');

        } catch(e) { console.error("연도 로딩 실패", e); }
    },

    // --- 3. 시험 목록 로딩 (검색 기능 포함) ---
    async loadExams() {
        if(!this.selectedSubjectId) return;

        const yearVal = document.getElementById('yearFilter').value;
        // yearVal이 있으면 ?year=2024, 없으면 파라미터 없음(전체)
        const query = yearVal ? `?year=${yearVal}` : '';

        try {
            const res = await fetch(`/admin/subject/${this.selectedSubjectId}/exam${query}`);
            const list = await res.json();
            this.renderList(list);
        } catch(e) { console.error(e); }
    },

    // 연도 드롭다운 변경 시 호출됨
    filterByYear() {
        this.loadExams();
    },

    renderList(list) {
        const tbody = document.getElementById('examList');
        const emptyMsg = document.getElementById('emptyMsg');

        if(list.length === 0) {
            tbody.innerHTML = '';
            emptyMsg.classList.remove('d-none');
            return;
        }
        emptyMsg.classList.add('d-none');

        tbody.innerHTML = list.map(exam => `
            <tr>
                <td><span class="badge bg-light text-dark border">${exam.year}</span></td>
                <td>
                    <a href="javascript:void(0);" onclick="examApp.goToProblem(${exam.id}, '${exam.name}', ${exam.year})" class="fw-bold text-decoration-none">
                        ${exam.name}
                    </a>
                </td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-info me-1" onclick='examApp.openUpdateModal(${JSON.stringify(exam)})'>
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="examApp.deleteExam(${exam.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `).join('');
    },

    // --- CRUD Actions (기존과 동일) ---
    openAddModal() {
        if(!this.selectedSubjectId) return alert("과목을 먼저 선택해주세요.");
        document.getElementById('addModalSubjectName').value = this.selectedSubjectName;
        document.getElementById('examName').value = '';
        document.getElementById('examYear').value = new Date().getFullYear();
        new bootstrap.Modal(document.getElementById('add-exam-modal')).show();
    },

    async saveExam() {
        const name = document.getElementById('examName').value;
        const year = document.getElementById('examYear').value;

        try {
            const res = await fetch(`/admin/exam?subjectId=${this.selectedSubjectId}`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({name, year: parseInt(year)})
            });
            if(res.ok) {
                bootstrap.Modal.getInstance(document.getElementById('add-exam-modal')).hide();
                this.loadYears(); // 연도가 추가되었을 수 있으니 갱신
                this.loadExams();
            }
        } catch(e) { alert("저장 실패"); }
    },

    openUpdateModal(exam) {
        this.selectedExamId = exam.id;
        document.getElementById('updateExamName').value = exam.name;
        document.getElementById('updateExamYear').value = exam.year;
        new bootstrap.Modal(document.getElementById('update-exam-modal')).show();
    },

    async updateExam() {
        const name = document.getElementById('updateExamName').value;
        const year = document.getElementById('updateExamYear').value;

        try {
            const res = await fetch(`/admin/exam/${this.selectedExamId}`, {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({name, year: parseInt(year)})
            });
            if(res.ok) {
                bootstrap.Modal.getInstance(document.getElementById('update-exam-modal')).hide();
                this.loadYears(); // 연도가 바뀌었을 수 있으니 갱신
                this.loadExams();
            }
        } catch(e) { alert("수정 실패"); }
    },

    async deleteExam(id) {
        if(!confirm("삭제하시겠습니까?")) return;
        try {
            const res = await fetch(`/admin/exam/${id}`, { method: 'DELETE' });
            if(res.ok) {
                this.loadYears(); // 연도가 사라졌을 수 있으니 갱신
                this.loadExams();
            }
        } catch(e) { alert("삭제 실패"); }
    },

    goToProblem(id, name, year) {
        sessionStorage.setItem("examId", id);
        sessionStorage.setItem("examName", name);
        sessionStorage.setItem("examYear", year);
        location.href = "/admin/problem";
    }
};
document.addEventListener('DOMContentLoaded', () => examApp.init());