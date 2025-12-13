document.addEventListener('DOMContentLoaded', () => {
    const answerForm = document.getElementById('answer-form');
    const questionId = document.getElementById('questionId').value;

    // 답변 등록
    answerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const contentArea = document.getElementById('answer-content');
        const content = contentArea.value.trim();

        if (!content) return alert('내용을 입력해주세요.');

        try {
            const res = await fetch(`/admin/question/${questionId}/answer`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({ content })
            });

            if (res.ok) {
                location.reload(); // 새로고침하여 목록 갱신
            } else {
                alert('등록 실패');
            }
        } catch (e) { console.error(e); alert('오류 발생'); }
    });

    // 이벤트 위임 (수정/삭제 버튼)
    document.getElementById('answerListArea').addEventListener('click', async (e) => {
        const target = e.target;
        const card = target.closest('.card');
        if (!card) return;

        const answerId = card.dataset.id;

        // 삭제
        if (target.classList.contains('answer-delete-btn')) {
            e.preventDefault();
            if (!confirm('정말 삭제하시겠습니까?')) return;

            try {
                const res = await fetch(`/admin/answer/${answerId}`, { method: 'DELETE' });
                if (res.ok) location.reload();
            } catch (e) { alert('삭제 오류'); }
        }

        // 수정 모드 진입
        if (target.classList.contains('answer-update-btn')) {
            e.preventDefault();
            const contentDiv = card.querySelector('.answer-content');
            const currentText = contentDiv.textContent;

            // 이미 수정 모드라면 패스
            if (card.querySelector('textarea')) return;

            const editArea = document.createElement('div');
            editArea.innerHTML = `
                <textarea class="form-control mb-2" rows="4">${currentText}</textarea>
                <div class="text-end">
                    <button class="btn btn-sm btn-secondary cancel-edit">취소</button>
                    <button class="btn btn-sm btn-primary save-edit">저장</button>
                </div>
            `;

            contentDiv.style.display = 'none';
            card.querySelector('.card-body').appendChild(editArea);

            // 취소 버튼
            editArea.querySelector('.cancel-edit').onclick = () => {
                editArea.remove();
                contentDiv.style.display = 'block';
            };

            // 저장 버튼
            editArea.querySelector('.save-edit').onclick = async () => {
                const newContent = editArea.querySelector('textarea').value.trim();
                if(!newContent) return alert("내용을 입력하세요");

                try {
                    const res = await fetch(`/admin/answer/${answerId}`, {
                        method: 'PATCH',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify({ content: newContent })
                    });
                    if(res.ok) location.reload();
                } catch(e) { alert('수정 오류'); }
            };
        }
    });
});