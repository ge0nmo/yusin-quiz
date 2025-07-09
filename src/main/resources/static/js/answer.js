const answerForm = document.getElementById('answer-form');
const deleteBtnList = document.querySelectorAll('.answer-delete-btn');
const updateBtnList = document.querySelectorAll('.answer-update-btn');
const questionId = document.getElementById('questionId').value;

// Initialize - load answers on page load
window.onload = function() {
    answerForm.addEventListener('submit', handlerRegisterAnswer);

    deleteBtnList.forEach((btn) => {
        const answerId = btn.closest('.card').getAttribute('value');
        btn.addEventListener('click', (e) => handleDeleteClick(e, answerId));
    });

    updateBtnList.forEach((btn) => {
        const answerId = btn.closest('.card').getAttribute('value');
        btn.addEventListener('click', (e) => handleUpdateClick(e, answerId));
    });
}

async function handlerRegisterAnswer(e) {
    e.preventDefault();
    const content = document.getElementById('answer-content').value.trim();

    if (!content) {
        alert('답변 내용을 입력해주세요');
        return;
    }

    try {
        await postJSON(`/admin/question/${questionId}/answer`, { content });
        resetAnswerForm(document.getElementById('answer-content'));
        window.location.reload();
    } catch (error) {
        console.error('Error registering answer:', error);
        alert('답변 등록에 실패했습니다.');
    }
}

async function handleDeleteClick(e, answerId) {
    e.preventDefault();

    if (confirm('정말 삭제하시겠습니까?')) {
        try {
            await deleteJSON(`/admin/answer/${answerId}`);
            window.location.reload();
        } catch (error) {
            console.error('Error deleting answer:', error);
            alert('답변 삭제에 실패했습니다.');
        }
    }
}

async function handleUpdateClick(e, answerId) {
    e.preventDefault();
    const cardBody = e.target.closest('.card-body');
    const contentDiv = cardBody.querySelector('.answer-content');
    const updateBtn = e.target;

    // If we're already in edit mode, return
    if (updateBtn.textContent === '저장') {
        return;
    }

    const currentContent = contentDiv.textContent.trim();

    const textarea = document.createElement('textarea');
    textarea.className = 'form-control mb-2';
    textarea.value = currentContent;
    textarea.rows = 3;

    contentDiv.style.display = 'none';
    cardBody.insertBefore(textarea, contentDiv.nextSibling);

    updateBtn.textContent = '저장';
    updateBtn.classList.remove('btn-outline-primary');
    updateBtn.classList.add('btn-primary');

    const saveHandler = async (saveEvent) => {
        saveEvent.preventDefault();
        const newContent = textarea.value.trim();

        if (!newContent) {
            alert('답변 내용을 입력해주세요.');
            return;
        }

        try {
            await patchJSON(`/admin/answer/${answerId}`, { content: newContent });

            contentDiv.textContent = newContent;
            contentDiv.style.display = '';
            textarea.remove();

            updateBtn.textContent = '수정';
            updateBtn.classList.remove('btn-primary');
            updateBtn.classList.add('btn-outline-primary');

            updateBtn.removeEventListener('click', saveHandler);
        } catch (error) {
            console.error('Update error:', error);
            alert('답변 수정에 실패했습니다.');
        }
    };

    updateBtn.removeEventListener('click', handleUpdateClick);
    updateBtn.addEventListener('click', saveHandler);
}

function resetAnswerForm(contentElement) {
    contentElement.value = '';
}

const postJSON = async function(url, uploadData) {
    return await sendJson('POST', url, uploadData);
}

const patchJSON = async function(url, uploadData) {
    return await sendJson('PATCH', url, uploadData);
}

const sendJson = async function(method, url, uploadData) {
    try {
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(uploadData),
        });

        if (!response.ok) {
            const data = await response.json();
            throw new Error(`${data.message || 'API Error'} (${response.status})`);
        }

        return await response.json();
    } catch (err) {
        console.error('API Error:', err);
        throw err;
    }
}

const deleteJSON = async function(url) {
    try {
        const response = await fetch(url, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return response;
    } catch (err) {
        console.error('Delete Error:', err);
        throw err;
    }
}