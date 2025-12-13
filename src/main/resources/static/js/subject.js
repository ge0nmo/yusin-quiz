const subjectApp = {
    selectedSubjectId: null,

    init(initialData) {
        if (initialData && initialData.response) {
            this.renderPage(initialData.response);
        } else {
            // 데이터가 없으면 Fetch (API가 필요하다면)
        }
    },

    renderPage(pageData) {
        const list = pageData.content;
        const tbody = document.getElementById('subjectList');

        if (list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center">등록된 과목이 없습니다.</td></tr>';
            return;
        }

        tbody.innerHTML = list.map(sub => `
            <tr>
                <td>${sub.id}</td>
                <td>
                    <a href="javascript:void(0);" onclick="subjectApp.goToExam(${sub.id}, '${sub.name}')" 
                       class="text-decoration-none fw-bold text-dark d-block">
                        <i class="fas fa-book me-2 text-primary"></i>${sub.name}
                    </a>
                </td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-info me-1" onclick="subjectApp.openUpdateModal(${sub.id}, '${sub.name}')">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="subjectApp.deleteSubject(${sub.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `).join('');

        // 페이징 로직 (간소화)
        // this.renderPagination(pageData);
    },

    async saveSubject() {
        const name = document.getElementById('subjectName').value;
        if(!name) return alert("과목명을 입력하세요");

        try {
            const res = await fetch('/admin/subject', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({name})
            });
            if(res.ok) location.reload();
        } catch(e) { console.error(e); }
    },

    openUpdateModal(id, name) {
        this.selectedSubjectId = id;
        document.getElementById('updateSubjectName').value = name;
        new bootstrap.Modal(document.getElementById('update-subject-modal')).show();
    },

    async updateSubject() {
        const name = document.getElementById('updateSubjectName').value;
        try {
            const res = await fetch(`/admin/subject/${this.selectedSubjectId}`, {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({name})
            });
            if(res.ok) location.reload();
        } catch(e) { console.error(e); }
    },

    async deleteSubject(id) {
        if(!confirm("삭제하시겠습니까?")) return;
        try {
            const res = await fetch(`/admin/subject/${id}`, { method: 'DELETE' });
            if(res.ok) location.reload();
        } catch(e) { console.error(e); }
    },

    goToExam(id, name) {
        sessionStorage.setItem("subjectId", id);
        sessionStorage.setItem("subjectName", name);
        location.href = "/admin/exam";
    }
};
window.subjectApp = subjectApp;