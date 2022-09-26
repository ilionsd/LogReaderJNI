//
// Created by ilion on 21-Sep-22.
//

#include <cassert>
#include <cstring>
#include <memory>

#include "LogReader.h"

class Token;
class WildcardToken;
class QuestionmarkToken;
class SubstringToken;

CLogReader::CLogReader()
    : m_mutexIsRunning()
    , m_mutexTokens()
    , m_mutexLines()
    , m_mutexMatches()
    , m_bIsRunning(false)
    , m_Worker()
    , m_Tokens()
    , m_Lines()
    , m_Matches()
{}

bool CLogReader::IsRunning() const {
    const std::lock_guard<std::mutex> lockIsRunning(m_mutexIsRunning);
    return m_bIsRunning;
}

bool CLogReader::SetFilter(const char *filter) {
    const std::lock_guard<std::mutex> lock(m_mutexTokens);
    m_Tokens.clear();
    size_t length = strlen(filter);
    size_t pos = 0;
    while (pos < length) {
        if (filter[pos] == '*') {
            // Wildcard
            size_t k = pos;
            while (k < length && filter[k++] == '*');
            std::unique_ptr<Token> token = std::make_unique<WildcardToken>();
            m_Tokens.push_back(std::move(token));
            pos = k;
        }
        else if (filter[pos] == '?') {
            // Questionmark
            std::unique_ptr<Token> token = std::make_unique<QuestionmarkToken>();
            m_Tokens.push_back(std::move(token));
            pos++;
        }
        else {
            // Substring
            size_t k = pos;
            while (k < length && (filter[k] != '*' && filter[k] != '?')) {
                k++;
            }
            std::unique_ptr<Token> token = std::make_unique<SubstringToken>(filter + pos, k - 1);
            m_Tokens.push_back(std::move(token));
            pos = k;
        }
    }
    return true;
}

bool CLogReader::AddSourceBlock(const char *block, const size_t size) {
    const std::lock_guard<std::mutex> lock(m_mutexLines);
    size_t prev = 0;
    for (size_t pos = 0; pos < size; pos++) {
        if (block[pos] == '\n') {
            std::unique_ptr<char[]> pStr = std::make_unique<char[]>(pos - prev + 1);
            memcpy(pStr.get(), block + prev, pos - prev);
            prev = pos;
            m_Lines.push(std::move(pStr));
        }
    }
    if (!IsRunning()) {
        StartProcessing();
    }
    return true;
}

std::vector<std::unique_ptr<char[]>> CLogReader::GetMatches() {
    const std::lock_guard<std::mutex> lockMatches(m_mutexMatches);
    return std::move(m_Matches);
}

void CLogReader::DoWork() {
    {
        const std::lock_guard<std::mutex> lockIsRunning(m_mutexIsRunning);
        m_bIsRunning = true;
    }
    const std::lock_guard<std::mutex> lockTokens(m_mutexTokens);
    while (true) {
        std::unique_ptr<char[]> line;
        {
            const std::lock_guard<std::mutex> lockLines(m_mutexLines);
            if (m_Lines.empty()) {
                const std::lock_guard<std::mutex> lockIsRunning(m_mutexIsRunning);
                m_bIsRunning = false;
                break;
            }
            line = std::move(m_Lines.front());
            m_Lines.pop();
        }
        bool matches = ProcessLine(line.get());
        if (matches) {
            const std::lock_guard<std::mutex> lockMatches(m_mutexMatches);
            m_Matches.push_back(std::move(line));
        }
    }
}

bool CLogReader::ProcessLine(const char *line, const size_t index) const {
    if (index == m_Tokens.size()) {
        return true;
    }
    else {
        std::pair<const char *, const char *> matching(nullptr, line);
        do {
            matching = m_Tokens[index]->Matches(matching.second);
            if (matching.first) {
                bool result = ProcessLine(matching.first, index + 1);
                if (result) {
                    return true;
                }
            }
        }
        while (matching.second);
        return false;
    }
}

bool CLogReader::StartProcessing() {
    m_Worker = std::thread(&CLogReader::DoWork, this);
    return true;
}

std::pair<const char *, const char *>
WildcardToken::Matches(const char *line) const {
    return std::make_pair(line, nullptr);
}

std::pair<const char *, const char *>
QuestionmarkToken::Matches(const char *line) const {
    return (strlen(line) > 1)
        ? std::make_pair(line + 1, nullptr)
        : std::make_pair(nullptr, nullptr);
}

SubstringToken::SubstringToken(const char *line, const size_t count) {
    assert((count < BUFFER_SIZE));
    strncpy(m_pStr, line, count);
}

std::pair<const char *, const char *>
SubstringToken::Matches(const char *line) const {
    const char *occurrence = strstr(line, m_pStr);
    return (occurrence)
        ? std::make_pair(occurrence + strlen(m_pStr), occurrence + 1)
        : std::make_pair(nullptr, nullptr);
}

