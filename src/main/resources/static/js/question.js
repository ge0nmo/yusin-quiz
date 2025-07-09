let currentPage = 1;
const pageSize = 10;
const prevPage = document.querySelector('.prevPage');
const nextPage = document.querySelector('.nextPage');
const questionList = document.querySelector('#questionList');

// Initialize page
window.onload = function() {
    loadPage();
    initializePagination();
};

function initializePagination() {
    prevPage.addEventListener('click', handlePrevPage);
    nextPage.addEventListener('click', handleNextPage);
}

async function loadPage() {
    try {
        showLoadingState();
        const data = await getJSON(`/admin/question/list?page=${currentPage - 1}&size=${pageSize}`);
        renderQuestionList(data.data);
        updatePaginationButtons(data.pageInfo);
    } catch (err) {
        console.error('Error loading page:', err);
        showErrorState();
    } finally {
        hideLoadingState();
    }
}

function renderQuestionList(questions) {
    questionList.innerHTML = '';

    if (!questions || questions.length === 0) {
        showEmptyState();
        return;
    }

    const fragment = document.createDocumentFragment();

    questions.forEach(question => {
        const row = createQuestionRow(question);
        fragment.appendChild(row);
    });

    questionList.appendChild(fragment);
}

function createQuestionRow(question) {
    const row = document.createElement('tr');
    if (question.answeredByAdmin) {
        row.classList.add('table-success');
    }

    row.innerHTML = `
        <td class="text-nowrap">${escapeHtml(question.username)}</td>
        <td>
            <a href="/admin/question/${question.id}/answer" class="question-link">
                ${escapeHtml(question.title)}
            </a>
        </td>
        <td class="text-nowrap">${formatDate(question.createdAt)}</td>
    `;

    return row;
}

function formatDate(dateString) {
    try {
        const date = new Date(dateString);
        return new Intl.DateTimeFormat('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        }).format(date);
    } catch (e) {
        console.error('Date formatting error:', e);
        return dateString;
    }
}

function updatePaginationButtons(pageInfo) {
    const prevPageItem = prevPage.parentElement;
    const nextPageItem = nextPage.parentElement;

    prevPageItem.classList.toggle('disabled', pageInfo.currentPage <= 1);
    nextPageItem.classList.toggle('disabled', pageInfo.currentPage >= pageInfo.totalPages);

    // Update aria-disabled for accessibility
    prevPage.setAttribute('aria-disabled', pageInfo.currentPage <= 1);
    nextPage.setAttribute('aria-disabled', pageInfo.currentPage >= pageInfo.totalPages);
}

function handlePrevPage(e) {
    e.preventDefault();
    if (currentPage > 1) {
        currentPage--;
        loadPage();
    }
}

function handleNextPage(e) {
    e.preventDefault();
    if (!e.target.closest('.page-item').classList.contains('disabled')) {
        currentPage++;
        loadPage();
    }
}

// Utility Functions
function showLoadingState() {
    questionList.innerHTML = `
        <tr>
            <td colspan="3" class="text-center py-4">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
            </td>
        </tr>
    `;
}

function hideLoadingState() {
    // Loading state will be replaced by actual content
}

function showErrorState() {
    questionList.innerHTML = `
        <tr>
            <td colspan="3" class="text-center text-danger py-4">
                <i class="fas fa-exclamation-circle me-2"></i>데이터를 불러오는데 실패했습니다.
            </td>
        </tr>
    `;
}

function showEmptyState() {
    questionList.innerHTML = `
        <tr>
            <td colspan="3" class="text-center text-muted py-4">
                <i class="fas fa-inbox me-2"></i>등록된 질문이 없습니다.
            </td>
        </tr>
    `;
}

function escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// API Call
const getJSON = async function(url) {
    try {
        const response = await fetch(url);
        const data = await response.json();

        if (!response.ok) {
            throw new Error(`${data.message || 'API Error'} (${response.status})`);
        }

        return data;
    } catch (err) {
        console.error('API Error:', err);
        throw err;
    }
};