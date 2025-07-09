const examApp = {
    selectedSubjectId: null,
    selectedSubjectName: null,
    selectedYear: null,
    selectedExamId: null,
    yearDropdown: document.querySelector('#year-dropdown'),
    examNameInput: document.querySelector('#examName'),

    async init() {
        this.selectedSubjectId = sessionStorage.getItem("subjectId");
        this.selectedSubjectName = sessionStorage.getItem("subjectName");

        await this.addHandlerSubjectDropdown();

        if(this.selectedSubjectId && this.selectedSubjectName) {
            sessionStorage.removeItem("subjectId");
            sessionStorage.removeItem("subjectName");
            await this.addHandlerYearDropdown();
        }
    },

    async addHandlerSubjectDropdown() {
        const dropdown = document.querySelector('#subject-dropdown-content');
        const subjectDropdownButton = document.querySelector('#subject-dropdown');
        dropdown.innerHTML = '';

        try {
            const response = await fetch("/admin/subject/list");
            const data = await response.json();

            if (this.selectedSubjectId && this.selectedSubjectName) {
                subjectDropdownButton.textContent = this.selectedSubjectName;
            } else if (data.length > 0) {
                this.selectedSubjectId = data[0].id;
                this.selectedSubjectName = data[0].name;
                subjectDropdownButton.textContent = this.selectedSubjectName;
                await this.addHandlerYearDropdown();
            } else {
                subjectDropdownButton.textContent = '과목 없음';
                subjectDropdownButton.disabled = true;
            }

            data.forEach((subject) => {
                const button = document.createElement('button');
                button.className = 'dropdown-item';
                button.type = 'button';
                button.textContent = subject.name;
                button.addEventListener('click', () => this.addHandlerSelectSubject(subject.id, subject.name));
                dropdown.appendChild(button);
            });
        } catch (error) {
            console.error('Error fetching subjects:', error);
        }
    },

    async addHandlerSelectSubject(subjectId, subjectName) {
        this.clearYearDropdown();
        this.selectedSubjectId = subjectId;
        this.selectedSubjectName = subjectName;
        document.querySelector('#subject-dropdown').textContent = this.selectedSubjectName;
        await this.addHandlerYearDropdown();
    },

    async addHandlerYearDropdown() {
        const dropdown = document.querySelector('#year-content');
        dropdown.innerHTML = '';

        if (!this.selectedSubjectId) return;

        try {
            const response = await fetch(`/admin/exam/year?subjectId=${this.selectedSubjectId}`);
            const data = await response.json();

            if (data.length > 0) {
                this.selectedYear = data[0];
                this.yearDropdown.textContent = this.selectedYear;
                const examList = await this.getExamList(this.selectedSubjectId, this.selectedYear);
                this.loadData(examList);
            } else {
                this.yearDropdown.textContent = '연도 없음';
                this.loadData([]);
            }

            data.forEach((year) => {
                const button = document.createElement('button');
                button.className = 'dropdown-item';
                button.type = 'button';
                button.textContent = year;
                button.addEventListener('click', () => this.addHandlerSelectYear(year));
                dropdown.appendChild(button);
            });
        } catch (error) {
            console.error('Error fetching years:', error);
        }
    },

    clearYearDropdown() {
        this.yearDropdown.textContent = '연도 선택';
        document.querySelector('#year-content').innerHTML = '';
        this.selectedYear = null;
        this.loadData([]);
    },

    async addHandlerSelectYear(year) {
        this.selectedYear = year;
        this.yearDropdown.textContent = this.selectedYear;

        if (this.selectedSubjectId && this.selectedYear) {
            const examList = await this.getExamList(this.selectedSubjectId, this.selectedYear);
            this.loadData(examList);
        } else {
            this.loadData([]);
        }
    },

    prepareExamForm() {
        document.querySelectorAll('.selectedSubjectName').forEach((input) => {
            input.value = this.selectedSubjectName || '';
        });

        this.examNameInput.value = '';
        document.querySelector('#examYear').value = this.selectedYear || new Date().getFullYear();
    },

    prepareUpdateExamForm(exam) {
        document.querySelectorAll('.selectedSubjectName').forEach((input) => {
            input.value = this.selectedSubjectName || '';
        });

        document.querySelector('#newExamName').value = exam.name;
        document.querySelector('#newExamYear').value = exam.year;
        this.selectedExamId = exam.id;
    },

    loadData(examList) {
        const examListElement = document.getElementById('examList');
        if (!examList || examList.length === 0) {
            examListElement.innerHTML = `
                <tr>
                    <td colspan="2" class="text-center py-4">등록된 시험이 없습니다.</td>
                </tr>
            `;
            return;
        }
        this.drawList(examList);
    },

    drawList(list) {
        const html = list.map(exam => `
            <tr>
                <td>${exam.year}</td>
                <td>
                    <div class="d-flex justify-content-between align-items-center">
                        ${exam.name}
                        <div class="d-flex justify-content-end">
                            <button onclick='examApp.prepareUpdateExamForm(${JSON.stringify(exam)})' 
                                    data-bs-toggle="modal" 
                                    data-bs-target="#update-exam-modal" 
                                    class="btn btn-link p-0 me-2" 
                                    type="button">
                                <img alt="update" src="/static/img/edit.svg" width="20" height="20">
                            </button>
                            <button onclick="examApp.removeExam(${exam.id})" 
                                    class="btn btn-link p-0" 
                                    type="button">
                                <img alt="remove" src="/static/img/trash.svg" width="20" height="20">
                            </button>
                        </div>
                    </div>
                </td>
            </tr>
        `).join('');

        document.getElementById('examList').innerHTML = html;
    },

    async removeExam(examId) {
        if (!confirm('정말 삭제하시겠습니까?')) {
            return;
        }

        try {
            const response = await fetch(`/admin/exam/${examId}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error('삭제에 실패했습니다');
            }

            const examList = await this.getExamList(this.selectedSubjectId, this.selectedYear);
            this.loadData(examList);
            await this.addHandlerYearDropdown();
        } catch (error) {
            console.error("Error removing exam:", error);
            alert("시험 삭제 중 오류가 발생했습니다.");
        }
    },

    async updateExam() {
        const examName = document.querySelector('#newExamName').value;
        const examYear = document.querySelector('#newExamYear').value;

        const examUpdateRequest = {
            name: examName,
            year: Number(examYear)
        };

        try {
            const response = await fetch(`/admin/exam/${this.selectedExamId}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(examUpdateRequest)
            });

            if (!response.ok) {
                throw new Error('수정에 실패했습니다.');
            }

            const examList = await this.getExamList(this.selectedSubjectId, this.selectedYear);
            this.hideModal('update-exam-modal');
            this.loadData(examList);
            await this.addHandlerYearDropdown();
        } catch (error) {
            console.error("Error updating exam:", error);
            alert("시험 정보 수정 중 오류가 발생했습니다.");
        }
    },

    async saveExam() {
        const name = this.examNameInput.value;
        const year = Number(document.querySelector('#examYear').value);

        if (!this.selectedSubjectId) {
            alert("과목을 먼저 선택해주세요.");
            return;
        }

        const examCreateRequest = {
            name: name,
            year: year
        };

        this.selectedYear = year;

        try {
            const response = await fetch(`/admin/exam?subjectId=${this.selectedSubjectId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(examCreateRequest)
            });

            if (!response.ok) {
                throw new Error('저장에 실패했습니다.');
            }

            const examList = await this.getExamList(this.selectedSubjectId, this.selectedYear);
            this.yearDropdown.textContent = this.selectedYear;
            this.hideModal('add-exam-modal');
            this.loadData(examList);
            await this.addHandlerYearDropdown();
        } catch (error) {
            console.error("Error saving exam:", error);
            alert("시험 저장 중 오류가 발생했습니다.");
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

    async getExamList(subjectId, year) {
        if (!subjectId) return [];

        try {
            const response = await fetch(`/admin/subject/${subjectId}/exam?year=${year}`);
            return await response.json();
        } catch (error) {
            console.error("Error fetching exam list:", error);
            return [];
        }
    }
};

// Initialize the app
window.examApp = examApp;
document.addEventListener('DOMContentLoaded', () => examApp.init());