const answerDetail = document.querySelector('.answer-detail');
const questionId = document.getElementById('questionId').value;
let currentPage = 0;
const size = 10;

const getJSON = async function(url, options = {}) {
    try {
        const res = await fetch(url, options);
        const data = await res.json();
        if (!res.ok) throw new Error(`${data.message} (${res.status})`);
        return data;
    } catch (err) {
        console.error('Error fetching data:', err);
        throw err;
    }
};

// Function to format date
const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    }).replace(/\//g, '-');
};

// Function to render answers
const renderAnswers = (answers) => {
    let html = `
        <h5 class="font-weight-bold m-3">답변 목록</h5>
    `;

    if (answers.length === 0) {
        html += `<div class="alert alert-info m-3">아직 답변이 없습니다.</div>`;
    } else {
        answers.forEach(answer => {
            html += `
                <div class="answer-item border-bottom p-3">
                    <div class="answer-user-info d-flex justify-content-sm-start align-items-center">
                        <p class="m-1 mr-2 font-weight-bold">${answer.username}</p>
                        <p class="m-1 ml-2 font-weight-light">${formatDate(answer.createdAt)}</p>
                    </div>
                    <div class="answer-content mt-2">
                        <p>${answer.content}</p>
                    </div>
                </div>
            `;
        });
    }

    return html;
};

// Function to render pagination
const renderPagination = (pageInfo) => {
    if (pageInfo.totalPages <= 1) return '';

    let html = `
        <nav class="mt-4">
            <ul class="pagination justify-content-center">
    `;

    // Previous button
    html += `
        <li class="page-item ${pageInfo.currentPage === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${pageInfo.currentPage - 2}" aria-label="Previous">
                <span aria-hidden="true">&laquo;</span>
            </a>
        </li>
    `;

    // Page numbers
    for (let i = 1; i <= pageInfo.totalPages; i++) {
        html += `
            <li class="page-item ${i === pageInfo.currentPage ? 'active' : ''}">
                <a class="page-link" href="#" data-page="${i - 1}">${i}</a>
            </li>
        `;
    }

    // Next button
    html += `
        <li class="page-item ${pageInfo.currentPage === pageInfo.totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${pageInfo.currentPage}" aria-label="Next">
                <span aria-hidden="true">&raquo;</span>
            </a>
        </li>
    `;

    html += `
            </ul>
        </nav>
    `;

    return html;
};

// Function to render answer form
const renderAnswerForm = () => {
    return `
        <div class="answer-form-container card shadow mb-4 mt-4">
            <div class="card-header py-3">
                <h6 class="m-0 font-weight-bold text-primary">답변 작성</h6>
            </div>
            <div class="card-body">
                <form id="answer-form">
                
                    <div class="form-group">
                        <textarea class="form-control" id="content" name="content" rows="5" placeholder="답변 내용" required></textarea>
                    </div>
                    
                    <button type="submit" class="btn btn-primary float-right">
                        답변 등록
                    </button>
                    
                </form>
            </div>
        </div>
    `;
};

// Function to submit new answer
const submitAnswer = async (formData) => {
    try {
        const response = await getJSON(`/admin/question/${questionId}/answer`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        });

        // Reload answers to show the new one
        loadAnswers();

        // Reset form
        document.getElementById('answer-form').reset();

        // Show success message
        alert('답변이 성공적으로 등록되었습니다.');

    } catch (err) {
        console.error('Error submitting answer:', err);
        alert('답변 등록 중 오류가 발생했습니다. 나중에 다시 시도해주세요.');
    }
};

// Function to load answers
const loadAnswers = async (page = 0) => {
    try {
        const response = await getJSON(`/admin/question/${questionId}/answer/list?page=${page}&size=${size}`);
        const { data, pageInfo } = response;

        // Update current page
        currentPage = pageInfo.currentPage - 1;

        // Render answers and pagination
        let html = renderAnswers(data);
        html += renderPagination(pageInfo);

        // Add answer form
        html += renderAnswerForm();

        answerDetail.innerHTML = html;

        // Add event listeners to pagination
        document.querySelectorAll('.pagination .page-link').forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                const page = parseInt(this.dataset.page);
                loadAnswers(page);
            });
        });

        // Add event listener to answer form
        document.getElementById('answer-form').addEventListener('submit', function(e) {
            e.preventDefault();

            const formData = {
                username: document.getElementById('username').value,
                password: document.getElementById('password').value,
                content: document.getElementById('content').value,
                questionId: questionId
            };

            submitAnswer(formData);
        });

    } catch (err) {
        console.error('Error loading answers:', err);
        answerDetail.innerHTML = `
            <div class="alert alert-danger m-3">
                답변을 불러오는 중 오류가 발생했습니다. 나중에 다시 시도해주세요.
            </div>
        `;
    }
};

// Initialize - load answers on page load
document.addEventListener('DOMContentLoaded', () => {
    loadAnswers();
});