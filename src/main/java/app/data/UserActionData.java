package app.data;

public enum UserActionData {

    USER_SAVED,//п впервые сохранен в базу после подписки на публичные каналы
    USER_HAD_START_SUCCESS,//п вызвал команду старт - не является стартом бота
    USER_HAD_REFERRAL_START,//п выполнил команду старт с реферальной ссылкой - сохраняет пользователя и обновляет реферала. НЕ является подпиской на канал


    INITIAL_SUBSCRIBE_CHECK,//п нажал кнопку проверить подписку на партнеров (впервые - первичная проверка. Вторично и дале вышел из канала партнеров и использует бота)
    CHECK_SUBSCRIBE_FAIL,//п не подписан на партнеров
    CHECK_SUBSCRIBE_SUCCESS,//п подписался на партнеров


    JOIN_PRIVATE_CHANNEL,//п присоединился к приватному каналу
    LEFT_PRIVATE_CHANNEL,//п покинул приватный канал


    LEFT_PUBLIC_CHANNEL,//п покинул публичный канал
    RETURN_PUBLIC_CHANNEL_48H,//п вернулся в публичный канал - обнаружено через 48 часов
    REMOVE_PRIVATE_CHANNEL_48H,//п удален из приватного канала так как не вернулся через 48 часов в публичный


    GET_SCHEDULE_MSG_IS_ARE_YOU_OK_3H,//получил запланированное сообщение активации через 3 часа
    GET_SCHEDULE_MSG_IS_ARE_YOU_OK_24H,//получил запланированное сообщение активации через 24 часа
    GET_SCHEDULE_MSG_IS_SHARE_WITH_FRIENDS_72H_7D_14D,//получил запланированное сообщение поделиться с другом через промежуток времени
    GET_SCHEDULE_COMPLETE,//больше нет запланированных сообщений для п


    THE_USER_GET_NEW_REFERRAL,//у п появился новый реферал
    SHARE_PERSONAL_CONTACT_WITH_BOT,//п поделился контактом для запроса помощи
    OPEN_MAIN_MENU,//открыл меню
    OPEN_MY_BOLLS,//открыл меню баллов
    OPEN_SHARE,//открыл меню поделиться
    OPEN_SPEND_BOLLS_MENU,//открыл меню вознаграждений

    REQUEST_AWARD_YES,//запросил вознаграждение за бонусы
    REQUEST_AWARD_NO,//запросил вознаграждение за бонусы, а оно еще не доступно для него

    ARE_YOU_OK_YES,//подтвердил что целевое действие успешно выполнено
    ARE_YOU_OK_HELP,//сообщил что требуется помощь для достижения целевого действия
    ARE_YOU_OK_WAIT,//сообщил что не приступал к достижению целевого действие


    //пока что не анализируем действия администраторов
    OPEN_ADMIN_PANEL_SUCCESS_BY_CALLBACK,
    OPEN_ADMIN_PANEL_SUCCESS,
    OPEN_ADMIN_PANEL_FAIL,

    WATCHING_WELCOME_MESSAGE,
    START_EDIT_WELCOME_MESSAGE,
    SAVED_NEW_WELCOME_MESSAGE,
    START_EDIT_UTM,
    ADDING_NEW_UTM_START,
    WATCHING_UTM_LIST,
    SAVE_NEW_UTM,
    EDITING_WELCOME_MESSAGE
}