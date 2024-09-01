import React, { useState, ChangeEvent } from 'react';
import Paper from '@mui/material/Paper';
import InputBase from '@mui/material/InputBase';
import Divider from '@mui/material/Divider';
import IconButton from '@mui/material/IconButton';
import MenuIcon from '@mui/icons-material/Menu';
import SearchIcon from '@mui/icons-material/Search';
import DirectionsIcon from '@mui/icons-material/Directions';

interface SearchBarProps {
    onSearch: (searchTerm: string) => void;
  }
  
const SearchBar: React.FC<SearchBarProps> = ({ onSearch }) => {
  const [searchTerm, setSearchTerm] = useState('');

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(event.target.value);
    // if (onSearch) {
    //   onSearch(event.target.value);
    // }
  };

  return (
    <Paper
      component="form"
      sx={{ p: '2px 4px', m: 'auto 2px', display: 'flex', alignItems: 'center', flex: 1 }}
    >
      {/* <IconButton sx={{ p: '10px' }} aria-label="menu">
        <MenuIcon />
      </IconButton> */}
      <InputBase
        sx={{ m: 1, flex: 1 }}
        placeholder="검색하세요"
        value={searchTerm}
        onChange={handleChange}
        inputProps={{ 'aria-label': 'search transactions' }}
      />
      <IconButton type="button" onClick={() => {onSearch(searchTerm)}} sx={{ p: '10px' }} aria-label="search">
        <SearchIcon />
      </IconButton>
      {/* <Divider sx={{ height: 28, m: 0.5 }} orientation="vertical" /> */}
      {/* <IconButton color="primary" sx={{ p: '10px' }} aria-label="directions">
        <DirectionsIcon />
      </IconButton> */}
    </Paper>
  );
}

export default SearchBar;