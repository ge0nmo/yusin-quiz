import View from "../../View.js";


class SubjectResultView extends View{
    _parentElement = document.querySelector('#subjectList');
    _errorMessage = '과목 데이터가 존재하지 않습니다.';
    _message = '';

    addHandlerRender(handler){
        handler();
    }


    _generateMarkup(){
        console.log('로그');
        const list = this._data;
        let html = '';
        list.forEach((subject) => {
            html += `
                <tr>
                    <td>${subject.id}</td>
                    <td>
                        <div class="d-flex justify-content-between">
                            <span>
                                ${subject.name}
                            </span>
                            <div class="d-flex justify-content-end">
                                <button class="btn btn-default" type="button">
                                    <img data-toggle="modal" data-target="#update-subject-modal"
                                    alt="update" src="/img/edit.svg">
                                </button>
                                <button class="btn btn-default" type="button">
                                    <img alt="remove" src="/img/trash.svg">
                                </button>
                            </div>
                        </div>
                    </td>
                </tr>
            `;
        });
        return html;
    }
}

export default new SubjectResultView();