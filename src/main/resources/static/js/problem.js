const problemApp = {
    selectedSubjectId: null,
    selectedSubjectName: null,
    selectedYear: null,
    selectedExamId: null,
    selectedExamName: null,

    summernoteOption: {
        toolbar: [
            ['fontname', ['fontname']],
            ['fontsize', ['fontsize']],
            ['style', ['bold', 'italic', 'underline', 'strikethrough', 'clear']],
            ['color', ['forecolor', 'color']],
            ['table', ['table']],
            ['para', ['ul', 'ol', 'paragraph']],
            ['height', ['height']],
            ['insert', ['picture', 'link', 'video']],
            ['view', ['fullscreen', 'help']]
        ],
        lang: 'ko-KR',
        fontNames: ['Arial', 'Arial Black', 'Comic Sans MS', 'Courier New', '맑은 고딕', '궁서', '굴림체', '굴림', '돋움체', '바탕체'],
        fontSizes: ['8', '9', '10', '11', '12', '14', '16', '18', '20', '22', '24', '28', '30', '36', '50', '72'],
        height: 200,
        callbacks: {
            onImageUpload: function(files) {
                problemApp.uploadImageToServer(files[0], $(this));
            }
        }
    },

    async init() {
        await new Promise(resolve => {
            const checkDependencies = () => {
                if (window.jQuery && jQuery.fn.summernote) {
                    resolve();
                } else {
                    setTimeout(checkDependencies, 100);
                }
            };
            checkDependencies();
        });

        this.initializeEventListeners();
        await this.addHandlerSubjectDropdown();

        try {
            this.initializeSummernote();
        } catch (error) {
            console.error('Error initializing Summernote:', error);
        }
    },

    initializeEventListeners() {
        document.querySelector('.search-click').addEventListener('click', () => this.addHandlerSearchClick());
        document.querySelector('.add-problem-button').addEventListener('click', () => this.prepareProblemForm());
        document.querySelector('.problem-save-button').addEventListener('click', () => this.addHandlerProblemSaveClick());
        document.getElementById('addChoiceBtn').addEventListener('click', () => this.addNewChoiceRow());
        document.getElementById('examList').addEventListener('change', (e) => this.handleExamSelection(e));
    },

    initializeSummernote() {
        if (typeof $.fn.summernote === 'undefined') {
            console.error('Summernote is not loaded');
            return;
        }

        try {
            $('#problemContent').summernote(this.summernoteOption);
            $('#problemExplanation').summernote(this.summernoteOption);
        } catch (error) {
            console.error('Error initializing Summernote:', error);
        }
    },

    async addHandlerSubjectDropdown() {
        const dropdown = document.querySelector('#subject-dropdown-content');
        dropdown.innerHTML = '';

        try {
            const data = await this.getJSON("/admin/subject/list");
            data.forEach((subject) => {
                const button = document.createElement('button');
                button.className = 'dropdown-item';
                button.textContent = subject.name;
                button.addEventListener('click', () => this.addHandlerSelectSubject(subject.id, subject.name));
                dropdown.appendChild(button);
            });
        } catch (error) {
            console.error('Error loading subjects:', error);
            alert('과목 목록을 불러오는데 실패했습니다.');
        }
    },

    async addHandlerSelectSubject(subjectId, subjectName) {
        this.clearYearDropdown();
        this.clearExamDropdown();

        this.selectedSubjectId = subjectId;
        this.selectedSubjectName = subjectName;
        document.querySelector('#subject-dropdown').textContent = this.selectedSubjectName;

        await this.addHandlerYearDropdown();
    },

    async addHandlerYearDropdown() {
        const dropdown = document.querySelector('#year-content');
        dropdown.innerHTML = '';

        try {
            const data = await this.getJSON(`/admin/exam/year?subjectId=${this.selectedSubjectId}`);
            data.forEach(year => {
                const button = document.createElement('button');
                button.className = 'dropdown-item';
                button.textContent = year;
                button.addEventListener('click', () => this.addHandlerSelectYear(year));
                dropdown.appendChild(button);
            });
        } catch (error) {
            console.error('Error loading years:', error);
            alert('연도 목록을 불러오는데 실패했습니다.');
        }
    },

    async addHandlerSelectYear(year) {
        this.selectedYear = year;
        document.querySelector('#year-dropdown').textContent = this.selectedYear;

        if (this.selectedSubjectId && this.selectedYear) {
            await this.loadExamList();
        }
    },

    clearYearDropdown() {
        document.querySelector('#year-dropdown').textContent = '연도 선택';
        document.querySelector('#year-content').innerHTML = '';
        this.selectedYear = null;
    },

    clearExamDropdown() {
        const examList = document.getElementById('examList');
        examList.innerHTML = '<option value="">시험 선택</option>';
        this.selectedExamId = null;
    },

    async loadExamList() {
        const examList = document.getElementById('examList');
        try {
            const exams = await this.getJSON(`/admin/subject/${this.selectedSubjectId}/exam?year=${this.selectedYear}`);
            examList.innerHTML = '<option value="">시험 선택</option>';
            exams.forEach(exam => {
                const option = document.createElement('option');
                option.value = exam.id;
                option.textContent = exam.name;
                examList.appendChild(option);
            });
        } catch (error) {
            console.error('Error loading exams:', error);
            alert('시험 목록을 불러오는데 실패했습니다.');
        }
    },

    handleExamSelection(event) {
        this.selectedExamId = event.target.value || null;
        this.selectedExamName = event.target.options[event.target.selectedIndex].text;
    },

    async addHandlerSearchClick() {
        if (!this.selectedExamId) {
            alert('시험을 선택해주세요');
            return;
        }

        try {
            const problemList = await this.getJSON(`/admin/problem/list?examId=${this.selectedExamId}`);
            await this.loadProblemData(problemList);
        } catch (error) {
            console.error('Error searching problems:', error);
            alert('문제 목록을 불러오는데 실패했습니다.');
        }
    },

    async loadProblemData(problemList) {
        const examTable = document.getElementById('examTable');

        if (!problemList || problemList.length === 0) {
            examTable.innerHTML = `
                <div class="alert alert-info text-center">
                    등록된 문제가 없습니다.
                </div>
            `;
            return;
        }

        const html = problemList.map(problem => this.createProblemCard(problem)).join('');
        examTable.innerHTML = html;

        problemList.forEach(problem => {
            try {
                this.initializeProblemEditors(problem);
            } catch (error) {
                console.error(`Error initializing editors for problem ${problem.id}:`, error);
            }
        });
    },

    createProblemCard(problem) {
        return `
        <div id="problem-${problem.id}" class="card problem-card" data-problem-id="${problem.id}">
            <div class="card-header problem-header">
                <div class="header-content">
                    <div class="title-group">
                        <h5>문제 ${problem.number}</h5>
                        <input type="number" 
                               class="form-control form-control-sm problemNumber" 
                               value="${problem.number}" 
                               min="1"
                               max="40"
                               required>
                    </div>
                    <div class="action-buttons">
                        <button class="btn btn-light btn-icon problemEditBtn" 
                                onclick="problemApp.handleUpdateProblemClick(${problem.id})"
                                title="저장">
                            <i class="fas fa-save"></i>
                        </button>
                        <button class="btn btn-light btn-icon" 
                                onclick="problemApp.handleRemoveProblem(${problem.id})"
                                title="삭제">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            </div>
            <div class="card-body">
                <div class="summernote-container">
                    <textarea id="problemContent-${problem.id}" class="problemContent">${problem.content || ''}</textarea>
                </div>

                <div class="choices-section mb-4">
                    <div class="choices list-group mb-3">
                        ${this.loadChoiceData(problem.choices || [])}
                    </div>
                    <button class="btn btn-outline-primary w-100" 
                            onclick="problemApp.handleAddChoice(${problem.id})">
                        <i class="fas fa-plus"></i> 선택지 추가
                    </button>
                </div>

                <div class="explanation-section">
                    <label class="form-label fw-bold">문제 해설</label>
                    <textarea id="problemExplanation-${problem.id}" class="problemExplanation">${problem.explanation || ''}</textarea>
                </div>
            </div>
        </div>
    `;
    },

    loadChoiceData(choiceList) {
        return (choiceList || []).map(choice => `
        <div id="choice-${choice.id}" 
             class="choice-item" 
             data-choice-id="${choice.id}">
            <div class="input-group">
                <input type="number" 
                       class="form-control form-control-sm choice-number" 
                       value="${choice.number}"
                       min="1"
                       max="40"
                       required>
                <textarea class="form-control choice-content">${choice.content || ''}</textarea>
                <div class="input-group-text">
                    <div class="form-check">
                        <input class="form-check-input isAnswer" 
                               type="checkbox" 
                               id="checkBox-${choice.id}" 
                               ${choice.isAnswer ? 'checked' : ''}>
                        <label class="form-check-label" for="checkBox-${choice.id}">정답</label>
                    </div>
                </div>
                <div class="btn-group">
                    <button class="btn btn-outline-danger btn-sm remove-choice" 
                            onclick="problemApp.markChoiceAsRemoved(${choice.id})"
                            title="삭제">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        </div>
    `).join('');
    },

    initializeProblemEditors(problem) {
        if (typeof $.fn.summernote === 'undefined') {
            console.error('Summernote is not loaded');
            return;
        }

        try {
            $(`#problemContent-${problem.id}`).summernote(this.summernoteOption)
                .summernote('code', problem.content || '');
            $(`#problemExplanation-${problem.id}`).summernote(this.summernoteOption)
                .summernote('code', problem.explanation || '');
        } catch (error) {
            console.error(`Error initializing editors for problem ${problem.id}:`, error);
            throw error;
        }
    },

    prepareProblemForm() {
        document.getElementById('subjectTitle').value = this.selectedSubjectName || '';
        document.getElementById('examName').value = this.selectedExamName || '';
        document.getElementById('examYear').value = this.selectedYear || '';
        this.resetProblemForm();
    },

    resetProblemForm() {
        document.getElementById('problemNumber').value = '';
        if ($.fn.summernote) {
            $('#problemContent').summernote('code', '');
            $('#problemExplanation').summernote('code', '');
        }
        document.getElementById('choicesContainer').innerHTML = '';
        this.addDefaultChoiceRows();
    },

    addDefaultChoiceRows() {
        for (let i = 0; i < 5; i++) {
            this.addNewChoiceRow();
        }
    },

    addNewChoiceRow() {
        const choiceRow = document.createElement('div');
        choiceRow.className = 'choice-row';
        choiceRow.innerHTML = `
            <div class="input-group">
                <input type="number" 
                       class="form-control form-control-sm choice-number"
                       placeholder="번호" 
                       min="1"
                       max="40"
                       required>
                <input type="text" class="form-control choiceContent" placeholder="선택지 내용" required>
                <div class="input-group-text">
                    <div class="form-check">
                        <input class="form-check-input isAnswer" type="checkbox" id="choice-answer-${Date.now()}">
                        <label class="form-check-label" for="choice-answer-${Date.now()}">정답</label>
                    </div>
                </div>
                <button type="button" class="btn btn-outline-danger removeChoiceBtn">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        `;

        choiceRow.querySelector('.removeChoiceBtn').addEventListener('click', () => choiceRow.remove());
        document.getElementById('choicesContainer').appendChild(choiceRow);
    },

    async handleUpdateProblemClick(problemId) {
        try {
            const problemForm = document.getElementById(`problem-${problemId}`);
            const num = Number(problemForm.querySelector('.problemNumber').value);
            const content = $(`#problemContent-${problemId}`).summernote('code');
            const explanation = $(`#problemExplanation-${problemId}`).summernote('code');

            // 선택지 데이터 수집
            const choices = Array.from(problemForm.querySelectorAll('.choice-item')).map(choiceItem => {
                const choiceId = choiceItem.getAttribute('data-choice-id');
                return {
                    id: choiceId ? parseInt(choiceId) : null,
                    number: Number(choiceItem.querySelector('.choice-number').value),
                    content: choiceItem.querySelector('.choice-content').value,
                    isAnswer: choiceItem.querySelector('.isAnswer').checked,
                    removedYn: choiceItem.classList.contains('removed')
                };
            });

            const requestData = {
                id: problemId,
                number: num,
                content: content,
                explanation: explanation,
                choices: choices
            };

            const response = await fetch(`/admin/problem?examId=${this.selectedExamId}`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestData)
            });

            if (!response.ok) throw new Error('수정 실패');

            // 업데이트 후 문제 목록 새로고침
            const problemList = await this.getJSON(`/admin/problem/list?examId=${this.selectedExamId}`);
            await this.loadProblemData(problemList);

            alert('문제가 성공적으로 업데이트되었습니다.');
        } catch (error) {
            console.error('Error updating problem:', error);
            alert(error.message);
        }
    },

    async handleRemoveProblem(problemId) {
        if (!confirm('정말 이 문제를 삭제하시겠습니까?')) return;

        try {
            const response = await fetch(`/admin/problem/${problemId}?examId=${this.selectedExamId}`, {
                method: 'DELETE',
            });
            if (!response.ok) throw new Error('삭제 실패');

            document.getElementById(`problem-${problemId}`).remove();
            alert('문제가 삭제되었습니다.');
        } catch (error) {
            console.error('Error removing problem:', error);
            alert(error.message);
        }
    },

    async handleAddChoice(problemId) {
        const choicesContainer = document.querySelector(`#problem-${problemId} .choices`);
        const newChoiceDiv = document.createElement('div');
        newChoiceDiv.className = 'choice-item';
        newChoiceDiv.innerHTML = `
        <div class="input-group">
            <input type="number" 
                   class="form-control form-control-sm choice-number"
                   min="1"
                   max="40"
                   required>
            <textarea class="form-control choice-content"></textarea>
            <div class="input-group-text">
                <div class="form-check">
                    <input class="form-check-input isAnswer" type="checkbox">
                    <label class="form-check-label">정답</label>
                </div>
            </div>
            <div class="btn-group">
                <button class="btn btn-outline-primary btn-sm save-new-choice" title="저장">
                    <i class="fas fa-save"></i>
                </button>
                <button class="btn btn-outline-danger btn-sm remove-choice" title="삭제">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
    `;

        newChoiceDiv.querySelector('.save-new-choice').addEventListener('click',
            () => this.saveNewChoice(problemId, newChoiceDiv));
        newChoiceDiv.querySelector('.remove-choice').addEventListener('click',
            () => newChoiceDiv.remove());

        choicesContainer.appendChild(newChoiceDiv);
    },

    async saveNewChoice(problemId, choiceElement) {
        try {
            const num = Number(choiceElement.querySelector('.choice-number').value);
            const content = choiceElement.querySelector('.choice-content').value;
            const isAnswer = choiceElement.querySelector('.isAnswer').checked;

            const response = await fetch(`/admin/choice?problemId=${problemId}&examId=${this.selectedExamId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    number: num,
                    content: content,
                    isAnswer: isAnswer,
                }),
            });

            if (!response.ok) throw new Error('저장 실패');

            const choiceId = await response.json();
            this.updateChoiceElement(choiceElement, choiceId);
            alert('선택지가 저장되었습니다.');
        } catch (error) {
            console.error('Error saving choice:', error);
            alert(error.message);
        }
    },

    markChoiceAsRemoved(choiceId) {
        if (!confirm('정말 이 선택지를 삭제하시겠습니까?')) return;

        const choiceElement = document.getElementById(`choice-${choiceId}`);
        choiceElement.classList.add('removed');
        choiceElement.style.display = 'none';
    },

    updateChoiceElement(element, choiceId) {
        element.id = `choice-${choiceId}`;
        element.setAttribute('data-choice-id', choiceId);

        const saveBtn = element.querySelector('.save-new-choice');
        const removeBtn = element.querySelector('.remove-choice');

        saveBtn.onclick = () => this.handleUpdateChoiceClick(choiceId);
        removeBtn.onclick = () => this.handleRemoveChoiceClick(choiceId);
    },

    async handleUpdateChoiceClick(choiceId) {
        try {
            const choiceElement = document.getElementById(`choice-${choiceId}`);
            const problemId = choiceElement.closest('.problem-card').getAttribute('data-problem-id');

            const data = {
                number: Number(choiceElement.querySelector('.choice-number').value),
                content: choiceElement.querySelector('.choice-content').value,
                isAnswer: choiceElement.querySelector('.isAnswer').checked
            };

            const response = await fetch(`/admin/choice/${choiceId}?problemId=${problemId}&examId=${this.selectedExamId}`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            if (!response.ok) throw new Error('수정 실패');
            alert('선택지가 성공적으로 업데이트되었습니다.');
        } catch (error) {
            console.error('Error updating choice:', error);
            alert('선택지 수정에 실패했습니다.');
        }
    },

    async handleRemoveChoiceClick(choiceId) {
        if (!confirm('정말 이 선택지를 삭제하시겠습니까?')) return;

        try {
            const choiceElement = document.getElementById(`choice-${choiceId}`);
            const problemId = choiceElement.closest('.problem-card').getAttribute('data-problem-id');

            const response = await fetch(`/admin/choice/${choiceId}?problemId=${problemId}&examId=${this.selectedExamId}`, {
                method: 'DELETE'
            });

            if (!response.ok) throw new Error('삭제 실패');

            choiceElement.remove();
            alert('선택지가 삭제되었습니다.');
        } catch (error) {
            console.error('Error removing choice:', error);
            alert('선택지 삭제에 실패했습니다.');
        }
    },

    async addHandlerProblemSaveClick() {
        try {
            const problemNumber = Number(document.getElementById('problemNumber').value);
            const problemContent = $('#problemContent').summernote('code');
            const explanation = $('#problemExplanation').summernote('code');

            const choiceCreateRequests = Array.from(document.querySelectorAll('.choice-row'))
                .map((row, index) => {
                    const content = row.querySelector('.choiceContent').value.trim();
                    if (!content) return null;

                    return {
                        number: index + 1,
                        content: content,
                        isAnswer: row.querySelector('.isAnswer').checked,
                    };
                })
                .filter(choice => choice !== null);

            if (!choiceCreateRequests.some(choice => choice.isAnswer)) {
                throw new Error('정답을 선택해주세요');
            }

            await this.saveProblem({
                number: problemNumber,
                content: problemContent,
                explanation: explanation,
                choices: choiceCreateRequests
            });

            const problemList = await this.getJSON(`/admin/problem/list?examId=${this.selectedExamId}`);
            await this.loadProblemData(problemList);
            this.hideModal('add-problem-modal');
            alert('저장되었습니다.');
        } catch (error) {
            alert(error.message);
        }
    },

    async saveProblem(problemData) {
        const response = await fetch(`/admin/problem?examId=${this.selectedExamId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(problemData),
        });

        if (!response.ok) throw new Error('저장 실패');
    },

    hideModal(modalId) {
        const modal = document.getElementById(modalId);
        const bsModal = bootstrap.Modal.getInstance(modal);
        if (bsModal) bsModal.hide();
    },

    async getJSON(url) {
        const response = await fetch(url);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    },

    uploadImageToServer(file, summernoteEditor) {
        const formData = new FormData();
        formData.append('file', file);

        fetch('/admin/file', {
            method: 'POST',
            body: formData,
        })
            .then(response => {
                if (!response.ok) throw new Error('Image upload failed');
                // Get the response text directly since it's a URL string
                return response.text();
            })
            .then(url => {
                // Insert the URL directly into summernote
                summernoteEditor.summernote('insertImage', url);
            })
            .catch(error => {
                console.error('Error uploading image:', error);
                alert('이미지 업로드에 실패했습니다.');
            });
    }
};

// Initialize the app and expose it globally
window.problemApp = problemApp;
document.addEventListener('DOMContentLoaded', () => problemApp.init());