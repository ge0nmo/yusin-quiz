
const subjectApp = {
    currentPage: 0,
    size: 10,
    selectedSubjectId: null,
    initialData: null,

    init(initialData) {
        this.initialData = initialData;
        this.loadData();
    },

    loadData() {
        const { response, params } = this.initialData;
        const { content, ...pagination } = response;
        this.drawList(content);
        this.drawPage(pagination, params);
    },

    drawList(list) {
        const html = list.map(subject => `
            <tr>
                <td>${subject.id}</td>
                <td>
                    <div class="d-flex justify-content-between align-items-center">
                        <span style="cursor: pointer" onclick="subjectApp.redirectToExam(${subject.id}, '${subject.name}')">
                            ${subject.name}
                        </span>
                        <div class="d-flex justify-content-end">
                            <button class="btn btn-link p-0 me-2" type="button">
                                <img onclick="subjectApp.prepareUpdateSubjectForm(${subject.id}, '${subject.name}')" 
                                     data-bs-toggle="modal" 
                                     data-bs-target="#update-subject-modal"
                                     alt="update" 
                                     src="/static/img/edit.svg" 
                                     width="20" 
                                     height="20">
                            </button>
                            <button onclick="subjectApp.removeSubject(${subject.id})" 
                                    class="btn btn-link p-0" 
                                    type="button">
                                <img alt="remove" 
                                     src="/static/img/trash.svg" 
                                     width="20" 
                                     height="20">
                            </button>
                        </div>
                    </div>
                </td>
            </tr>
        `).join('');

        document.getElementById('subjectList').innerHTML = html;
    },

    drawPage(pagination, params) {
        if (!pagination || !params) {
            document.querySelector('.paging').innerHTML = '';
            return;
        }

        this.currentPage = pagination.pageable.pageNumber;
        const totalPages = pagination.totalPages;

        const html = this.generatePaginationHtml(totalPages);
        document.querySelector('.paging').innerHTML = html;
    },

    generatePaginationHtml(totalPages) {
        let html = '<ul class="pagination">';

        // First page button
        if (this.currentPage > 0) {
            html += `
                <li class="page-item">
                    <a class="page-link" href="javascript:void(0);" onclick="subjectApp.movePage(0)">첫 페이지</a>
                </li>
            `;
        }

        // Page numbers
        const startPage = Math.max(0, this.currentPage - 2);
        const endPage = Math.min(totalPages - 1, this.currentPage + 2);

        for (let i = startPage; i <= endPage; i++) {
            if (i === this.currentPage) {
                html += `
                    <li class="page-item active">
                        <a class="page-link">${i + 1} <span class="visually-hidden">(current)</span></a>
                    </li>
                `;
            } else {
                html += `
                    <li class="page-item">
                        <a class="page-link" href="javascript:void(0);" onclick="subjectApp.movePage(${i})">${i + 1}</a>
                    </li>
                `;
            }
        }

        // Last page button
        if (this.currentPage < totalPages - 1) {
            html += `
                <li class="page-item">
                    <a class="page-link" href="javascript:void(0);" onclick="subjectApp.movePage(${totalPages - 1})">마지막 페이지</a>
                </li>
            `;
        }

        html += '</ul>';
        return html;
    },

    async removeSubject(id) {
        if (!confirm('정말 삭제하시겠습니까?')) {
            return;
        }

        try {
            const response = await fetch(`/admin/subject/${id}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error('삭제에 실패했습니다');
            }

            this.reloadPage();
        } catch (error) {
            console.error('Error:', error);
            alert('삭제 중 오류가 발생했습니다.');
        }
    },

    prepareUpdateSubjectForm(subjectId, subjectName) {
        document.querySelector('#newSubjectName').value = subjectName;
        this.selectedSubjectId = subjectId;
    },

    async saveSubject() {
        const name = document.querySelector('#subjectName').value;

        try {
            const response = await fetch('/admin/subject', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ name })
            });

            if (!response.ok) {
                throw new Error('저장에 실패했습니다');
            }

            await response.json();
            this.reloadPage();
            this.hideModal('add-subject-modal');
        } catch (error) {
            console.error('Error:', error);
            alert('저장 중 오류가 발생했습니다.');
        }
    },

    async updateSubject() {
        const newSubjectName = document.querySelector('#newSubjectName').value;

        try {
            const response = await fetch(`/admin/subject/${this.selectedSubjectId}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ name: newSubjectName })
            });

            if (!response.ok) {
                throw new Error('수정에 실패했습니다');
            }

            this.reloadPage();
            this.hideModal('update-subject-modal');
        } catch (error) {
            console.error('Error:', error);
            alert('수정 중 오류가 발생했습니다.');
        }
    },

    hideModal(modalId) {
        const modalElement = document.getElementById(modalId);
        if (modalElement) {
            const modalInstance = bootstrap.Modal.getInstance(modalElement);
            if (modalInstance) {
                modalInstance.hide();
            }
        }
    },

    movePage(page) {
        const params = {
            page: page || 0,
            size: this.size
        };
        location.href = location.pathname + '?' + new URLSearchParams(params).toString();
    },

    reloadPage() {
        this.movePage(this.currentPage);
    },

    redirectToExam(subjectId, subjectName) {
        sessionStorage.setItem("subjectId", subjectId);
        sessionStorage.setItem("subjectName", subjectName);
        window.location.href = `${window.location.origin}/admin/exam`;
    }
};

// Export to window for HTML event handlers
window.subjectApp = subjectApp;