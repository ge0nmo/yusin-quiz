let selectedSubjectId;
let selectedSubjectName;
let selectedYear;
let selectedExamId;
const yearDropdown = document.querySelector('#year-dropdown');
const yearContent = document.querySelector('#year-content');
const examName = document.querySelector('#examName');

window.onload = async function(){
    selectedSubjectId = sessionStorage.getItem("subjectId");
    selectedSubjectName = sessionStorage.getItem("subjectName");

    await addHandlerSubjectDropdown();

    if(selectedSubjectId && selectedSubjectName){
        sessionStorage.removeItem("subjectId");
        sessionStorage.removeItem("subjectName");

        const examList = await getJSON(`/admin/subject/${selectedSubjectId}/exam?year=${selectedYear}`);
        loadData(examList);
    }
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
    yearDropdown.textContent = '연도 선택';
    document.querySelector('#year-content').innerHTML = '';
    selectedYear = null;
}


async function addHandlerSelectYear(year) {
    selectedYear = year;
    yearDropdown.textContent = selectedYear;

    if (selectedSubjectId && selectedYear) {
        const examList = await getJSON(`/admin/subject/${selectedSubjectId}/exam?year=${selectedYear}`);
        loadData(examList);
    }
}


function prepareExamForm(){
    document.querySelectorAll('.selectedSubjectName').forEach((input) => {
        input.value = selectedSubjectName;
    })

    document.querySelector('#examName').value = '';
    document.querySelector('#examYear').value = selectedYear;
}

function prepareUpdateExamForm(exam){
    document.querySelectorAll('.selectedSubjectName').forEach((input) => {
        input.value = selectedSubjectName;
    })

    document.querySelector('#newExamName').value = exam.name;
    document.querySelector('#newExamYear').value = exam.year;

    selectedExamId = exam.id;
}

function loadData(examList){
    if(!examList || examList.length === 0){
        document.getElementById('examList').innerHTML = `
                <tr>
                    <td colspan="2" class="text-center">등록된 시험이 없습니다.</td>
                </tr>
            `
    } else{
        drawList(examList);
    }
}

function drawList(list){
    let html = '';

    list.forEach((exam) => {
        html += `
                <tr>
                    <td>${exam.year}</td>
                    <td>
                    <div class="d-flex justify-content-between">
                        ${exam.name}
                        <div class="d-flex justify-content-end">
                            <button onclick='prepareUpdateExamForm(${JSON.stringify(exam)})' data-toggle="modal" data-target="#update-exam-modal" class="btn btn-default" type="button">
                                <img class="" alt="update" src="/static/img/edit.svg" >
                            </button>
                            <button onclick="removeExam(${exam.id})" class="btn btn-default" type="button">
                                <img class="" alt="remove" src="/static/img/trash.svg" >
                            </button>
                        </div>
                    </div>
                    </td>
                </tr>
            `;
    });

    document.getElementById('examList').innerHTML = html;
}


function removeExam(examId){
    if(!confirm('정말 삭제하시겠습니까?')){
        return;
    }
    fetch(`/admin/exam/${examId}`, {
        method: 'DELETE',
    })
        .then((res) => {
            if(!res.ok){
                throw new Error('삭제에 실패했습니다');
            }
            return getExamList(selectedSubjectId, selectedYear);
        })
        .then(examList => {
            loadData(examList);
        })
}


function updateExam(){
    const examName = document.querySelector('#newExamName').value
    const examYear = document.querySelector('#newExamYear').value

    const examUpdateRequest = {
        name: examName,
        year: examYear,
    }

    fetch(`/admin/exam/${selectedExamId}`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(examUpdateRequest),
    })
        .then(response => response.json())
        .then(() => {
            getExamList(selectedSubjectId, selectedYear)
                .then(examList => {
                    $('#update-exam-modal').modal('hide');
                    loadData(examList);
                })
        })

}


function saveExam(){
    const name = document.querySelector('#examName').value;
    const year = Number(document.querySelector('#examYear').value);


    const examCreateRequest = {
        name: name,
        year: year,
    }

    selectedYear = year;

    fetch(`/admin/exam?subjectId=${selectedSubjectId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(examCreateRequest),
    })
        .then(response => response.json())
        .then(() => {
            getExamList(selectedSubjectId, selectedYear)
                .then(examList => {
                    yearDropdown.textContent = selectedYear;

                    hideModal();

                    loadData(examList);
                })
        })
        .catch(error => {
            console.log(error);
        })
}


function hideModal(){
    $('#add-exam-modal').modal('hide');
}

function getExamList(subjectId, year) {
    if(!subjectId) return;

    return new Promise((resolve, reject) => {
        $.ajax({
            url: `/admin/subject/${subjectId}/exam?year=${year}`,
            type: 'GET',
            success: function(examList) {
                resolve(examList);
            },
            error: function(error) {
                reject(error);
            }
        });
    });
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
