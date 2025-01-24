let selectedSubjectId;
let selectedSubjectName;
let selectedYear;
let selectedExamId;
let selectedExamName;
let problemContentQuill;
let problemContentQuillInstances = {};
let problemExplanationQuillInstances = {};

let problemExplanationQuill;


const toolbarOptions = [
    ['bold', 'italic', 'underline', 'strike'],
    [{ 'header': 1 }, { 'header': 2 }],
    [{ 'list': 'ordered'}, { 'list': 'bullet' }],

    [{ 'color': [] }, { 'background': [] }],
    ['image', 'link'],
]

const quillOption = {
    modules: {
        toolbar: toolbarOptions
    },
    theme: 'snow'
};

window.onload = async function() {
    addHandlerChoiceButton();
    await addHandlerSubjectDropdown();

    problemContentQuill = new Quill('#problemContent', quillOption);
    problemExplanationQuill = new Quill('#problemExplanation', quillOption);

    problemContentQuill.getModule('toolbar').addHandler('image', selectLocalImage);
    problemExplanationQuill.getModule('toolbar').addHandler('image', selectLocalImage);

    document.querySelector('.search-click').addEventListener('click', () => addHandlerSearchClick());
    document.querySelector('.add-problem-button').addEventListener('click', () => prepareProblemForm());
    document.querySelector('.problem-save-button').addEventListener('click', () => addHandlerProblemSaveClick());
};

function addHandlerChoiceButton(){
    const choicesContainer = document.getElementById('choicesContainer');
    const addChoiceBtn = document.getElementById('addChoiceBtn');

    addChoiceBtn.addEventListener('click', function() {
        const choiceRow = document.createElement('div');
        choiceRow.className = 'choice-row input-group mb-2';


        choiceRow.innerHTML = `
            <input type="text" class="form-control choiceContent" required>
            <div class="input-group-append">
                <div class="input-group-text">
                    <input type="checkbox" class="isAnswer" aria-label="정답 체크">
                </div>
                <button class="btn btn-outline-danger remove-choice-btn" type="button">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        `;

        const removeBtn = choiceRow.querySelector('.remove-choice-btn');
        removeBtn.addEventListener('click', () => choiceRow.remove());

        choicesContainer.appendChild(choiceRow);
    });
}

function selectLocalImage() {
    const input = document.createElement('input');
    input.setAttribute('type', 'file');
    input.setAttribute('accept', 'image/*');
    input.click();

    input.onchange = async () => {
        const file = input.files[0];

        // Validate file
        if (!file.type.includes('image/')) {
            alert('Please select an image file');
            return;
        }

        if (file.size > 20 * 1024 * 1024) {
            alert('Image size should not exceed 20MB');
            return;
        }

        try {
            const formData = new FormData();
            formData.append('file', file);

            // Upload image using your API
            const response = await fetch('/admin/file', {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                throw new Error('Image upload failed');
            }

            const imageUrl = await response.text();

            // Insert image URL into editor
            const range = this.quill.getSelection(true);
            this.quill.insertEmbed(range.index, 'image', imageUrl);
            this.quill.setSelection(range.index + 1);

        } catch (error) {
            console.error('Error uploading image:', error);
            alert('Failed to upload image');
        }
    };
}

async function addHandlerSubjectDropdown(){
    const dropdown = document.querySelector('#subject-dropdown-content');

    const data = await getJSON("/admin/subject/list");

    data.forEach((subject) => {
        const listGroup = document.createElement('button');
        listGroup.className = 'list-group-item dropdown-item';
        listGroup.textContent = subject.name;
        dropdown.appendChild(listGroup);
        listGroup.addEventListener('click', () => addHandlerSelectSubject(subject.id, subject.name));
    });
}

async function addHandlerSelectSubject(subjectId, subjectName) {
    clearYearDropdown();
    clearExamDropdown();

    selectedSubjectId = subjectId;
    console.log('선택한 과목 id = ', selectedSubjectId);
    selectedSubjectName = subjectName;
    const subjectDropdown = document.querySelector('#subject-dropdown');

    subjectDropdown.textContent = selectedSubjectName;

    await addHandlerYearDropdown();
}

async function addHandlerYearDropdown(){
    console.log('addHandlerYearDropdown');
    const subjectId = selectedSubjectId;
    const dropdown = document.querySelector('#year-content');

    const data = await getJSON(`/admin/exam/year?subjectId=${subjectId}`);

    for(const year of data){
        const listGroup = document.createElement('button');
        listGroup.className = 'list-group-item dropdown-item';
        listGroup.textContent = year;
        dropdown.appendChild(listGroup);
        listGroup.addEventListener('click', () => addHandlerSelectYear(year));
    }

}

function clearYearDropdown(){
    document.querySelector('#year-dropdown').textContent = '연도 선택';
    document.querySelector('#year-content').innerHTML = '';
    selectedYear = null;
}

function clearExamDropdown(){
    const examList = document.getElementById('examList');
    examList.innerHTML = '';
    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = '시험 선택';
    examList.appendChild(defaultOption);
    selectedExamId = null;
}

async function addHandlerSelectYear(year) {
    selectedYear = year;
    document.querySelector('#year-dropdown').textContent = selectedYear;

    if (selectedSubjectId && selectedYear) {
        await loadExamList();
    }
}

async function loadExamList() {
    const parentElement = document.getElementById('examList');
    const exams = await getJSON(`/admin/subject/${selectedSubjectId}/exam?year=${selectedYear}`);

    parentElement.innerHTML = '';
    const defaultOption = document.createElement('option');
    defaultOption.value = "";
    defaultOption.textContent = "시험 선택";
    parentElement.appendChild(defaultOption);

    exams.forEach((exam) => {
        const option = document.createElement('option');
        option.value = exam.id;
        option.textContent = exam.name;
        parentElement.appendChild(option);
    });
}


document.getElementById('examList').addEventListener('change', function () {
    const selectedValue = this.value;

    selectedExamId = selectedValue || null;
    selectedExamName = this.selectedOptions[0].textContent || null;
})


async function addHandlerSearchClick() {
    if(!selectedExamId){
        alert('시험을 선택해주세요')
    }

    const problemList = await getJSON(`/admin/problem/list?examId=${selectedExamId}`);
    loadProblemData(problemList);
}


function loadProblemData(problemList) {
    const examTable = document.getElementById('examTable');
    examTable.innerHTML = '';
    let html = '';

    console.log('문제 = ', problemList);

    problemList.forEach((problem) => {
        const choiceList = problem.choices;
        const choiceHTML = loadChoiceData(choiceList);
        const problemId = problem.id;

        html += `
                <div id="problem-${problemId}" class="card shadow-sm mb-4" data-problem-id="${problemId}">
                    <div class="card-header bg-primary text-white">
                        <div class="d-flex justify-content-between">
                            <h5 class="mb-0">
                                문제<input class="ml-2 problemNumber" value="${problem.number}" style="width: 50px">
                            </h5>

                            <div class="d-flex justify-content-end">
                                <button class="problemEditBtn btn btn-outline-light" onclick="handleUpdateProblemClick(${problemId})">
                                    <img class="editImg" src="/img/save.svg" alt="">
                                </button>
                                <button class="removeBtn btn btn-outline-light" onclick="handleRemoveProblem(${problemId})">
                                    <img src="/img/trash.svg" alt="">
                                </button>
                            </div>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="mb-3">
                             <div id="problemContent-${problemId}" class="problemContent form-control"></div>
                        </div>

                        <div class="choices list-group">${choiceHTML}</div>

                         <div class="add-choice d-flex list-group-item list-group-item-action justify-content-center">
                            <button class="btn btn-outline-secondary" onclick="handleAddChoice(${problemId})">
                                <span>문항 추가</span>
                                <img src="/img/add_icon.png" alt="" >
                            </button>
                        </div>

                        <div class="list-group-item list-group-item-action problem-explanation-container">
                            <span>문제 해설</span>                        
                            <div id="problemExplanation-${problemId}" class="problemExplanation form-control">
                            
                            </div>                                                                                                                                           
                        </div>

                    </div>
                </div>
            `
    })

    examTable.innerHTML = html;

    problemList.forEach((problem) => {
        const problemContentQuillContainer = document.getElementById(`problemContent-${problem.id}`);
        const contentQuill = new Quill(problemContentQuillContainer, quillOption);
        contentQuill.getModule('toolbar').addHandler('image', selectLocalImage);
        contentQuill.root.innerHTML = problem.content;
        problemContentQuillInstances[problem.id] = contentQuill;


        const problemExplanationQuillContainer = document.getElementById(`problemExplanation-${problem.id}`);
        const explanationQuill = new Quill(problemExplanationQuillContainer, quillOption);
        explanationQuill.getModule('toolbar').addHandler('image', selectLocalImage);
        explanationQuill.root.innerHTML = problem.explanation;
        problemExplanationQuillInstances[problem.id] = explanationQuill;
    });
}



function loadChoiceData(choiceList){
    let html = ``;
    choiceList.forEach((choice) => {
        const choiceId = choice.id;
        html += `
                <div id="choice-${choiceId}" class="list-group-item list-group-item-action d-flex align-items-center choice-item " data-choice-id="${choiceId}">
                    <input type="number" class="choice-number mr-2 col-1" value="${choice.number}" placeholder="번호" style="width: 50px">
                    <input type="text" class="choice-content flex-grow-1" value="${choice.content}">

                    <input class="form-check-input isAnswer" type="checkbox" value="" id='checkBox=${choiceId}' ${choice.isAnswer ? 'checked' : ''}>

                    <div class="choice-btn ml-2">
                        <button class="choiceEditBtn btn btn-sm btn-outline-secondary p-1" onclick="handleUpdateChoiceClick(${choiceId})">
                            <img src="/img/save.svg" alt="" class="m-1">
                        </button>
                        <button class="btn btn-sm btn-outline-secondary p-1 isRemove" onclick="handleRemoveChoiceClick(${choiceId})">
                            <img src="/img/trash.svg" alt="" class="m-1">
                        </button>
                    </div>
                </div>
            `
    });

    return html;
}


function handleAddChoice(problemId){
    const problemForm = document.getElementById(`problem-${problemId}`);
    const choiceForms = problemForm.querySelector('.choices');

    const choiceDiv = document.createElement('div');
    choiceDiv.classList.add('list-group-item', 'list-group-item-action', 'd-flex', 'align-items-center', 'choice-item');
    choiceDiv.innerHTML += `
            <input type="number" class="choice-number mr-2 col-1" placeholder="번호" style="width: 50px">
            <input type="text" class="choice-content flex-grow-1" placeholder="내용">

            <input class="form-check-input isAnswer" type="checkbox" value="">

            <div class="choice-btn ml-2">
                <button class="choiceEditBtn btn btn-sm btn-outline-secondary p-1" onclick="addNewChoice(${problemId}, this)">
                    <img src="/img/save.svg" alt="" class="m-1">
                </button>
                <button class="btn btn-sm btn-outline-secondary p-1 isRemove" onclick="removeNewChoice(this)">
                    <img src="/img/trash.svg" alt="" class="m-1">
                </button>
            </div>
        `

    choiceForms.appendChild(choiceDiv);
}

function removeNewChoice(buttonElement){
    const choiceForm = buttonElement.closest('.choice-item');
    if(choiceForm){
        choiceForm.remove();
    }
}

async function addNewChoice(problemId, buttonElement){
    try{
        const choiceForm = buttonElement.closest('.choice-item');
        const num = Number(choiceForm.querySelector('.choice-number').value);
        const content = choiceForm.querySelector('.choice-content').value;
        const isAnswer = choiceForm.querySelector('.isAnswer').checked;

        const removeBtn = choiceForm.querySelector('.isRemove');

        const choiceCreateRequest = {
            number: num,
            content: content,
            isAnswer: isAnswer,
        };

        const response = await fetch(`/admin/choice?problemId=${problemId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(choiceCreateRequest),
        });

        if(!response.ok){
            return alert("오류 발생");
        }

        const choiceId = response.json();

        choiceForm.id = `choice-${choiceId}`;
        choiceForm.setAttribute('data-choice-id', choiceId);

        buttonElement.onclick = () => handleUpdateChoiceClick(choiceId);
        removeBtn.onclick = () => handleRemoveChoiceClick(choiceId);


    } catch (error){
        console.log(error);
    }

}

async function addHandlerProblemSaveClick(){
    try{
        const saveModal = document.getElementById('add-problem-modal');

        const numElement = document.getElementById('problemNumber');

        const problemNumber = Number(numElement.value);
        const problemContent = problemContentQuill.root.innerHTML;
        const explanation = problemExplanationQuill.root.innerHTML;


        console.log(`문제 내용 = ${problemContent}`);

        const choiceContainer = document.getElementById('choicesContainer');
        const choiceRows = choiceContainer.querySelectorAll('.choice-row');
        const choiceCreateRequest = [];
        let choiceNumber = 1;
        choiceRows.forEach((choiceRow) => {
            const isAnswer = choiceRow.querySelector('.isAnswer').checked;
            const choiceContent = choiceRow.querySelector('.choiceContent').value;

            const choice = {
                number: choiceNumber,
                content: choiceContent,
                isAnswer: isAnswer,
            };
            choiceCreateRequest.push(choice);
            choiceNumber++;
        });

        const problemCreateRequest = {
            number: problemNumber,
            content: problemContent,
            explanation: explanation,
            choices: choiceCreateRequest
        };

        await saveProblem(problemCreateRequest);

        const problemList = await getJSON(`/admin/problem/list?examId=${selectedExamId}`);


        loadProblemData(problemList);
        resetProblemContainer(numElement, choiceContainer);
    } catch (error){
        console.log(error);
        alert('문제 저장 실패');
    }
}

function resetProblemContainer(numElement, choiceContainer){
    numElement.value = '';
    problemContentQuill.root.innerHTML = '';
    choiceContainer.innerHTML = '';
}

async function saveProblem(problemCreateRequest){
    try{
        await fetch(`/admin/problem?examId=${selectedExamId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(problemCreateRequest),
        });

        $('#add-problem-modal').modal('hide');
    } catch (error){
        throw new error('');
    }

}

async function handleUpdateProblemClick(problemId){
    try{
        const problemForm = document.getElementById(`problem-${problemId}`);

        const num = Number(problemForm.querySelector('.problemNumber').value);
        const content = problemContentQuillInstances[problemId].root.innerHTML;
        const explanation = problemExplanationQuillInstances[problemId].root.innerHTML;



        const problemUpdateRequest = {
            number: num,
            content: content,
            explanation: explanation,
        }

        const response = await fetch(`/admin/problem/${problemId}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(problemUpdateRequest),
        })

        if(!response.ok){
            throw new Error('수정 실패');
        }

        const problemList = await getJSON(`/admin/problem/list?examId=${selectedExamId}`);
        loadProblemData(problemList);
    } catch (error){
        console.log(error);
        alert('문제 수정 중 오류가 발생했습니다.');
    }
}


async function handleUpdateChoiceClick(choiceId){
    try{
        await updateChoice(choiceId);
        const problemList = await getJSON(`/admin/problem/list?examId=${selectedExamId}`);
        loadProblemData(problemList);
    } catch (error){
        throw error();
    }

}


async function updateChoice(choiceId){
    try{
        const choiceForm = document.getElementById(`choice-${choiceId}`);

        const choiceNumber = Number(choiceForm.querySelector('.choice-number').value);
        const choiceContent = choiceForm.querySelector('.choice-content').value;
        const isAnswer = choiceForm.querySelector('.isAnswer').checked;

        const choiceUpdateRequest = {
            number: choiceNumber,
            content: choiceContent,
            isAnswer: isAnswer
        }

        const response = await fetch(`/admin/choice/${choiceId}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(choiceUpdateRequest),
        });

        if(!response.ok){
            throw new Error('에러발생');
        }
    } catch (error){
        console.log(error);
    }
}

async function handleRemoveChoiceClick(choiceId){
    try{
        if(!confirm("정말 삭제하시겠습니까?")){
            return;
        }

        const choiceItem = document.querySelector(`.choice-item[data-choice-id="${choiceId}"]`);

        const response = await fetch(`/admin/choice/${choiceId}`, {
            method: 'DELETE',
        });

        if(!response.ok){
            throw new Error('삭제 실패')
        }

        if(choiceItem){
            choiceItem.classList.remove('d-flex');
            choiceItem.classList.add('deleted', 'd-none');
        }

    } catch (error){
        console.log(error);
    }
}


async function handleRemoveProblem(problemId){
    try{
        console.log('삭제 클릭');
        if(!confirm('정말 삭제하시겠습니까?')){
            return;
        }

        await fetch(`/admin/problem/${problemId}`, {
            method: 'DELETE',
        });

        const problemList = await getJSON(`/admin/problem/list?examId=${selectedExamId}`);
        loadProblemData(problemList);
        resetAddProblemModal();
    } catch(error){
        throw new Error('');
    }
}

function prepareProblemForm(){
    if(!selectedSubjectName || !selectedYear || !selectedExamName){
        alert("시험을 선택해주세요");

        return false;
    }

    const subjectTitle = document.getElementById('subjectTitle');
    const examName = document.getElementById('examName');
    const examYear = document.getElementById('examYear');

    subjectTitle.value = selectedSubjectName;
    examYear.value = selectedYear;
    examName.value = selectedExamName;
}

function resetAddProblemModal() {
    // Reset problem number
    document.getElementById('problemNumber').value = '';

    // Reset Quill content
    if (problemContentQuill) {
        problemContentQuill.root.innerHTML = '';
    }

    // Reset explanation quill container
    if(problemExplanationQuill){
        problemExplanationQuill.root.innerHTML = '';
    }

    // Clear choices container
    const choiceContainer = document.getElementById('choicesContainer');
    choiceContainer.innerHTML = '';
}

const getJSON  = async function(url){
    try{
        const res = await fetch(url);
        const data = await res.json();

        if (!res.ok) {
            throw new Error(`${data.message} (${res.status})`);
        }
        return data;
    } catch (err){
        throw err
    }
}