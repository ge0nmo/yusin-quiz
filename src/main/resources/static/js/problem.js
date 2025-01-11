let selectedSubjectId;
let selectedSubjectName;
let selectedYear;
let selectedExamId;
let selectedExamName;
let problemContentQuill;
let quillInstances = {};

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

window.onload = function() {
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

    problemContentQuill = new Quill('#problemContent', quillOption);

    problemContentQuill.getModule('toolbar').addHandler('image', selectLocalImage);
};

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

function handleInput() {
    // 검색 결과를 넣을 창 선택
    const parentElement = document.querySelector('#search-result');
    // 검색 한 단어 선택
    const searchedWord = document.querySelector('.form-control').value;

    // 기존에 검색 결과 지우기
    parentElement.innerHTML = '';

    // 검색한 단어의 길이가 0(검색을 안한 경우) 그대로 반환
    if (searchedWord.length === 0) {
        return;
    }

    // api 검색
    $.ajax({
        url: '/admin/subject/dropdown',
        type: 'GET',
        data: {
            name: searchedWord,
        },
        success: function (subjects) {
            if (subjects.length > 0) {
                subjects.forEach((subject) => {
                    const listGroup = document.createElement('li');
                    listGroup.className = 'list-group-item';
                    listGroup.textContent = subject.name;
                    parentElement.appendChild(listGroup);
                    listGroup.addEventListener('click', () => selectSubject(subject.id, subject.name));
                });
            } else {
                const listGroup = document.createElement('li');
                listGroup.className = 'list-group-item';
                listGroup.textContent = '검색 결과가 없습니다';
                parentElement.appendChild(listGroup);
            }
        },

    })

}

function selectSubject(subjectId, subjectName) {
    selectedSubjectId = subjectId;
    selectedSubjectName = subjectName;
    document.querySelector('#subjectSearchBar').textContent = selectedSubjectName;

    clearYearDropdown();
    clearExamDropdown();
}
function clearYearDropdown(){

    const yearList = document.getElementById('yearList');
    yearList.value = '';
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

async function inputYearHandle() {
    selectedYear = document.getElementById('yearList').value;

    if (selectedSubjectId && selectedYear) {
        await loadExamList();
    }
}

async function loadExamList() {
    const parentElement = document.getElementById('examList');
    try {

        const exams = await fetchExamList();

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

    } catch (error) {
        console.error('Error loading exam list:', error);
    }
}


async function fetchExamList() {
    try {
        const response = await fetch(`/admin/subject/${selectedSubjectId}/exam?year=${selectedYear}`);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const examList = await response.json(); // Parse the JSON response
        console.log(`Fetched exam data =`, examList);
        return examList;
    } catch (error) {
        console.error(`Error fetching exam list:`, error);
        return [];
    }
}

document.getElementById('examList').addEventListener('change', function () {
    const selectedValue = this.value;

    selectedExamId = selectedValue || null;
    selectedExamName = this.selectedOptions[0].textContent || null;
})


async function handleSearchClick() {
    try {
        const problemList = await getProblemData(selectedExamId);
        loadProblemData(problemList);
    } catch (error) {
        console.error('Error fetching problem data:', error);
    }
}

async function getProblemData(examId){
    try{
        const response = await fetch(`/admin/problem/list?examId=${examId}`);
        if (!response.ok) {
            throw new Error(`error ${response.status}`);
        }
        return response.json();
    } catch (error) {
        console.log(error);
    }
}


function loadProblemData(problemList) {
    const examTable = document.getElementById('examTable');
    examTable.innerHTML = '';
    let html = '';

    console.log('문제 = ', problemList);

    problemList.forEach((problem) => {
        const choiceList = problem.choices;
        const imageHtml = problem.explanation ? `<img src="${problem.explanation}" alt="" class="explanation">` : "";
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
                        <div class="text-center card-img">
                            ${imageHtml}
                        </div>
                        <div class="list-group-item list-group-item-action d-flex">
                                                                                                 
                            <input type="file" class="form-control formFile" onchange="previewImage(this)"/>                           
                        </div>

                    </div>
                </div>
            `
    })

    examTable.innerHTML = html;

    problemList.forEach((problem) => {
        const quillContainer = document.getElementById(`problemContent-${problem.id}`);
        const quill = new Quill(quillContainer, quillOption);
        quill.getModule('toolbar').addHandler('image', selectLocalImage);


        quill.root.innerHTML = problem.content;
        quillInstances[problem.id] = quill;
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

async function handleSaveClick(){
    try{
        const saveModal = document.getElementById('add-problem-modal');

        const problemNumber = Number(document.getElementById('problemNumber').value);
        const problemContent = problemContentQuill.root.innerHTML;
        const explanationUrl = await uploadImage(saveModal);


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
            explanation: explanationUrl,
            choices: choiceCreateRequest
        };

        await saveProblem(problemCreateRequest);

        const problemList = await getProblemData(selectedExamId);


        loadProblemData(problemList);
    } catch (error){
        console.log(error);
        alert('문제 저장 실패');
    }
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
        const content = quillInstances[problemId].root.innerHTML;

        console.log('문제 ', content);

        const fileUrl = await uploadImage(problemForm);
        console.log(`파일 url=`, fileUrl);

        const problemUpdateRequest = {
            number: num,
            content: content,
            explanation: fileUrl,
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

        const problemList = await getProblemData(selectedExamId);
        loadProblemData(problemList);
    } catch (error){
        console.log(error);
        alert('문제 수정 중 오류가 발생했습니다.');
    }
}


async function handleUpdateChoiceClick(choiceId){
    try{
        await updateChoice(choiceId);
        const problemList = await getProblemData(selectedExamId);
        loadProblemData(problemList);
    } catch (error){
        throw error();
    }

}

async function uploadImage(problemForm){
    console.log(`문제 폼2 = ${problemForm}`);
    const fileInput = problemForm.querySelector('.formFile');
    const existingImageUrl = problemForm.querySelector(".explanation")?.getAttribute('src');

    if(!fileInput.files.length){
        return existingImageUrl;
    }

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);

    try{
        const response = await fetch(`/admin/file`, {
            method: 'POST',
            body: formData
        });

        if(!response.ok){
            console.log("파일 업로드 실패");
        }
        console.log("리스폰스 = ", response);

        const url = await response.text();
        console.log(url);
        return url;

    } catch (error){
        console.log(error);
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
        if(!confirm('정말 삭제하시겠습니까?')){
            return;
        }

        await fetch(`/admin/problem/${problemId}`, {
            method: 'DELETE',
        });

        const problemList = await getProblemData(selectedExamId);
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

function previewImage(input){
    const file = input.files[0];
    if(file){
        const reader = new FileReader();
        const previewContainer = input.closest('.card-body').querySelector('.card-img');

        reader.onload = function(e){
            const exitingImg = previewContainer.querySelector('.explanation');
            if(exitingImg){
                exitingImg.remove();
            }

            const img = document.createElement('img');
            img.src = e.target.result;
            img.classList.add('explanation', 'img-fluid');
            previewContainer.appendChild(img);
        }

        reader.readAsDataURL(file);
    }

}

function resetAddProblemModal() {
    // Reset problem number
    document.getElementById('problemNumber').value = '';

    // Reset Quill content
    if (problemContentQuill) {
        problemContentQuill.root.innerHTML = '';
    }

    // Clear choices container
    const choiceContainer = document.getElementById('choicesContainer');
    choiceContainer.innerHTML = '';

    // Reset file input
    const fileInput = document.querySelector('#add-problem-modal .formFile');
    if (fileInput) {
        fileInput.value = '';
    }

    // Remove any preview images
    const cardImg = document.querySelector('#add-problem-modal .card-img');
    if (cardImg) {
        cardImg.innerHTML = '';
    }
}