//
// Created by ilion on 21-Sep-22.
//

#ifndef LOGPARSER_LOGREADER_H
#define LOGPARSER_LOGREADER_H

#include <memory>
#include <mutex>
#include <queue>
#include <thread>
#include <vector>

class CLogReader;
class Token;
class WildcardToken;
class QuestionmarkToken;
class SubstringToken;

class Token {
public:
    Token() = default;

    virtual
    std::pair<const char *, const char *>
    Matches(const char *line) const = 0;

    virtual ~Token() = default;
};

class WildcardToken : public Token {
public:
    virtual
    std::pair<const char *, const char *>
    Matches(const char *line) const override;

    virtual ~WildcardToken() override = default;
};

class QuestionmarkToken : public Token {
public:
    virtual
    std::pair<const char *, const char *>
    Matches(const char *line) const override;

    virtual ~QuestionmarkToken() override = default;
};

class SubstringToken : public Token {
public:
    SubstringToken(const char *line, const size_t count);

    virtual
    std::pair<const char *, const char *>
    Matches(const char *line) const override;

    virtual ~SubstringToken() override = default;

private:
    static constexpr size_t BUFFER_SIZE = 1024;
    char m_pStr[BUFFER_SIZE] = {0};
};

class CLogReader {

public:
    CLogReader();

    ~CLogReader() = default;

    bool IsRunning() const;

    bool SetFilter(const char *filter);   // установка фильтра строк, false - ошибка
    bool AddSourceBlock(const char* block, const size_t block_size); // добавление очередного блока текстового файла

    std::vector<std::unique_ptr<char[]>> GetMatches();

private:
    void DoWork();
    bool StartProcessing();
    bool ProcessLine(const char *line, const size_t index = 0) const;

    mutable std::mutex m_mutexIsRunning;
    mutable std::mutex m_mutexTokens;
    mutable std::mutex m_mutexLines;
    mutable std::mutex m_mutexMatches;
    bool m_bIsRunning;
    std::thread m_Worker;
    std::vector<std::unique_ptr<Token>> m_Tokens;
    std::queue<std::unique_ptr<char[]>> m_Lines;
    std::vector<std::unique_ptr<char[]>> m_Matches;
};



#endif //LOGPARSER_LOGREADER_H
