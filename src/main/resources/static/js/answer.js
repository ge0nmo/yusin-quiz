const answerForm = document.getElementById('answer-form');
const deleteBtnList = document.querySelectorAll('.answer-delete-btn');
const updateBtnList = document.querySelectorAll('.answer-update-btn');

const questionId = document.getElementById('questionId').value;

// Initialize - load answers on page load
window.onload = function() {
    answerForm.addEventListener('submit', handlerRegisterAnswer);

    deleteBtnList.forEach((btn) => {
        const answerId = btn.closest('.answer-item').getAttribute('value');
        console.log('answerId = ',answerId);
        btn.addEventListener('click', (e) => handleDeleteClick(e, answerId))
    });

    updateBtnList.forEach((btn) => {
        const answerId = btn.closest('.answer-item').getAttribute('value');
        console.log(`answerId=, `, answerId);
        btn.addEventListener('click', (e) => handleUpdateClick(e, answerId))
    })
}

async function handlerRegisterAnswer(e){
    e.preventDefault();
    const content = document.getElementById('answer-content').value.trim();
    console.log(`answer content = `, content)

    if(!content){
        alert('답변 내용을 입력해주세요');
        return;
    }

    const request = {
        content: content
    };

    await postJSON(`/admin/question/${questionId}/answer`, request);

    resetAnswerForm(content);
    window.location.reload();
}

async function handleDeleteClick(e, answerId){
    e.preventDefault();

    console.log('answerId ', answerId);
    if(confirm('정말 삭제하시겠습니까?')){
        await deleteJSON(`/admin/answer/${answerId}`);
    }

    window.location.reload();
}

async function handleUpdateClick(e, answerId) {
    e.preventDefault();
    const answerItem = e.target.closest('.answer-item');
    const contentDiv = answerItem.querySelector('.answer-content');
    const currentContent = contentDiv.textContent.trim();
    const updateBtn = e.target;

    // Create editable textarea
    const textarea = document.createElement('textarea');
    textarea.className = 'form-control mb-2';
    textarea.value = currentContent;

    // Replace content with textarea
    contentDiv.replaceWith(textarea);
    updateBtn.textContent = '저장';

    // Remove previous event listener
    updateBtn.removeEventListener('click', handleUpdateClick);

    // Add temporary save handler
    const saveHandler = async (saveEvent) => {
        saveEvent.preventDefault();
        const newContent = textarea.value.trim();

        if (!newContent) {
            alert('답변 내용을 입력해주세요.');
            return;
        }

        try {
            const request = { content: newContent };
            await patchJSON(`/admin/answer/${answerId}`, request);

            // Create new content display
            const newContentDiv = document.createElement('div');
            newContentDiv.className = 'answer-content mt-2';
            newContentDiv.textContent = newContent;

            // Replace textarea with updated content
            textarea.replaceWith(newContentDiv);
            updateBtn.textContent = '수정';

            // Restore original event listener
            updateBtn.removeEventListener('click', saveHandler);
            updateBtn.addEventListener('click', (e) => handleUpdateClick(e, answerId));
        } catch (error) {
            alert('답변 수정에 실패했습니다.');
            console.error('Update error:', error);
        }
    };

    updateBtn.addEventListener('click', saveHandler);
}

function resetAnswerForm(contentElement){
    contentElement.value = '';
}

const postJSON = async function(url, uploadData){
    return await sendJson('POST', url, uploadData);
}

const patchJSON = async function(url, uploadData){
    return await sendJson('PATCH', url, uploadData);
}


const sendJson  = async function(method, url, uploadData){
    try{
        const fetchPro = fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(uploadData),
        });

        const res = await fetchPro;
        const data = await res.json();

        if (!res.ok) {
            throw new Error(`${data.message} (${res.status})`);
        }
        return data;
    } catch (err){
        throw err
    }
}

const getJSON = async function(url) {
    try {
        const res = await fetch(url);
        const data = await res.json();
        if (!res.ok) throw new Error(`${data.message} (${res.status})`);
        return data;
    } catch (err) {
        console.error('Error fetching data:', err);
        throw err;
    }
};

const deleteJSON = async function(url, id){
    try{
        const fetchPro = fetch(url, {
            method: 'DELETE',
        });

        const res = await fetchPro;
        if(!res.ok){
            throw new Error(`${data.message} (${res.status})`);
        }
    } catch (err){
        throw err;
    }

}