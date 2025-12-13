let currentPage = 0; // 0-based page index로 변경 (Spring Data Pageable 기본값)
const pageSize = 10;
const questionList = document.getElementById('questionList');
const loadingSpinner = document.getElementById('loadingSpinner');

window.onload = () => loadPage(0);

// 페이지 로드 함수
async function loadPage(page) {
    currentPage = page;
    loadingSpinner.style.display = 'block';
    questionList.innerHTML = ''; // 초기화

    try {
        const response = await fetch(`/admin/question/list?page=${currentPage}&size=${pageSize}&sort=createdAt,desc`);
        if (!response.ok) throw new Error('API Error');

        const result = await response.json();
        // GlobalResponse 구조 (data, pageInfo)에 맞춤
        renderList(result.data);
        updatePagination(result.pageInfo);

    } catch (e) {
        console.error(e);
        questionList.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">데이터 로드 실패</td></tr>`;
    } finally {
        loadingSpinner.style.display = 'none';
    }
}

function renderList(list) {
    if (!list || list.length === 0) {
        questionList.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-5"><i class="fas fa-inbox fa-3x mb-3 text-light"></i><br>등록된 질문이 없습니다.</td></tr>`;
        return;
    }

    questionList.innerHTML = list.map(q => `
        <tr onclick="location.href='/admin/question/${q.id}/answer'" style="cursor: pointer;">
            <td class="ps-4">
                ${q.answeredByAdmin
        ? '<span class="badge badge-soft-success rounded-pill px-3">답변완료</span>'
        : '<span class="badge badge-soft-warning rounded-pill px-3">미답변</span>'}
            </td>
            <td>
                <a href="/admin/question/${q.id}/answer" class="question-title text-dark fw-bold">
                    ${escapeHtml(q.title)}
                </a>
                <div class="small text-muted text-truncate" style="max-width: 500px;">${escapeHtml(q.content)}</div>
            </td>
            <td>
                <div class="d-flex align-items-center">
                    <div class="bg-light rounded-circle text-primary d-flex justify-content-center align-items-center me-2" style="width: 32px; height: 32px;">
                        <i class="fas fa-user small"></i>
                    </div>
                    <span>${escapeHtml(q.username)}</span>
                </div>
            </td>
            <td class="text-secondary small">${formatDate(q.createdAt)}</td>
            <td class="text-center">
                <button class="btn btn-sm btn-light text-primary">
                    <i class="fas fa-chevron-right"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function updatePagination(pageInfo) {
    const prevBtn = document.querySelector('.prevPage');
    const nextBtn = document.querySelector('.nextPage');

    // prevBtn
    prevBtn.onclick = (e) => { e.preventDefault(); if(pageInfo.currentPage > 1) loadPage(currentPage - 1); };
    prevBtn.parentElement.classList.toggle('disabled', pageInfo.currentPage <= 1);

    // nextBtn
    nextBtn.onclick = (e) => { e.preventDefault(); if(pageInfo.currentPage < pageInfo.totalPages) loadPage(currentPage + 1); };
    nextBtn.parentElement.classList.toggle('disabled', pageInfo.currentPage >= pageInfo.totalPages);
}

// Utils
function formatDate(dateStr) {
    if(!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' });
}

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/[&<>"']/g, function(m) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
    });
}